(ns chat.async
  (:require [clojure.core.async :as a]
            [ring.websocket :as ws]
            [ring.websocket.protocols :as wsp]
            [java-time.api :as jt]
            [taoensso.telemere :as t]
            [hkimjp.datascript :as ds]))

(defrecord Closed [code reason])

(defn closed
  "When a closed message is sent via the output channel, the websocket
  will close with the supplied integer code and reason string."
  [code reason]
  {:pre [(integer? code) (string? reason)]}
  (->Closed code reason))

(defn websocket-listener
  "Takes three core.async channels for input, output, and error reporting
  respectively, and returns a Ring websocket listener.

  Data sent to the 'in' channel will be sent to the client via the websocket.
  The 'out' channel will receive data sent from the client. The 'err' channel
  will deliver any Throwable exceptions that occur.

  Closing the out channel will close the socket and the two other channels."
  [in out err]
  (reify wsp/Listener
    (on-open [_ sock]
      (letfn [(fail [ex]
                (a/put! err ex))
              (out-loop []
                (a/take! out (fn [mesg]
                               (if (some? mesg)
                                 (if (instance? Closed mesg)
                                   (ws/close sock (:code mesg) (:reason mesg))
                                   (ws/send sock mesg out-loop fail))
                                 (ws/close sock)))))]
        (out-loop)))
    (on-message [_ _ mesg]
      ;; FIXME: patched by hkimura to record chats in datascript.
      ;; hkimura does not know how to read tagged literals.
      ;; to compare timestamps, timestamp must not be strings.
      ;; need improve.
      (t/log! :info mesg)
      (ds/put (assoc mesg :db/add -1 :timestamp (jt/local-date-time)))
      (a/put! in mesg))
    (on-pong [_ _ _]
      ; (t/log! :info "on-pong")
      nil)
    (on-error [_ _ ex]
      (t/log! {:level :info :data ex} "on-error")
      (a/put! err ex))
    (on-close [_ _ _ _]
      ; (t/log! :info "on-close")
      (a/close! in)
      (a/close! out)
      (a/close! err))))

(comment
  (ds/q '[:find (max ?e)
          :where
          [?e]])

  (ds/pull 5)
  (ds/q '[:find ?message
          :where
          [?e :message ?message]])
  :rtf)

(defmacro go-websocket
  "Macro for returning a websocket response handled by an inner go block.
  Expects three binding symbols - in, out and err - and assigns them to
  channels (see: websocket-listener). The body of the macro is executed in a
  core.async go block.

  The err symbol may optionally be omitted. In that case, any websocket
  error will result in the socket being closed with a 1011 unexpected
  server error response.

  Example:
  (go-websocket [in out err]
    (loop []
      (when-let [msg (<! in)]
        (>! out msg)
        (recur))))"
  {:clj-kondo/lint-as 'clojure.core/fn
   :arglists '([[in out] & body] [[in out err] & body])}
  [[in out err] & body]
  {:pre [(symbol? in) (symbol? out) (or (nil? err) (symbol? err))]}
  (if (some? err)
    `(let [~in  (a/chan)
           ~out (a/chan)
           ~err (a/chan)]
       (a/go ~@body)
       {::ws/listener (websocket-listener ~in ~out ~err)})
    `(let [~in  (a/chan)
           ~out (a/chan)
           err# (a/chan)]
       (a/go (when-let [ex# (a/<! err#)]
               (a/>! ~out (closed 1011 "Unexpected Error"))))
       (a/go ~@body)
       {::ws/listener (websocket-listener ~in ~out err#)})))

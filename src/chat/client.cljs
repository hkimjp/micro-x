(ns chat.client
  (:require [cljs.core.async :as a :refer [<! >! go go-loop]]
            [cljs-http.client :as http]
            [clojure.string :as str]
            [haslett.client :as ws]
            [haslett.format :as wsfmt]
            [taoensso.telemere :as t]))

(defn- query [query]
  (.querySelector js/document query))

(defn- admin? []
  (= (.-value (query "#author")) "hkimura"))

(defn- abbrev [s]
  (str (first s) "****"))

(defn- append-html [element html]
  ;; https://qiita.com/isseium/items/12b215b6eab26acd2afe
  (.play js/sound)
  (.insertAdjacentHTML element "afterbegin" html))

(defn- message-html [{:keys [author message]}]
  (str "<li><span class='date'>"
       (js/Date.)
        "<br><span class='author'>"
       (if (str/blank? author) "Anonymous" (abbrev author)) "</span>"
       "<span class='message'>" message "</span></li>"))

(defn- send-message [stream]
  (js/console.log "send-message")
  (let [message (query "#message")
        author  (query "#author")
        data    {:author  (.-value author)
                 :message (.-value message)}]
    (if (str/starts-with? (.-value message) "＠")
      (js/alert "全角の ＠ を使っています。")
      (do
        (go (>! (:out stream) data))
        (set! (.-value message) "")
        (.focus message)))))

(defn- dest [s]
  (re-find #"[^, ]+" (subs s 1)))

(defn- start-listener [stream message-log]
  (js/console.log "start-listener")
  (go-loop []
    (when-some [message (<! (:in stream))]
      (if (str/starts-with? (:message message) "@")
        (when (= (.-value (query "#author")) (dest (:message message)))
          (append-html message-log (message-html message)))
        (append-html message-log (message-html message)))
      (recur))))

(defn- websocket-url [path]
  (js/console.log "websocket-url")
  (let [loc   (.-location js/window)
        ;; fixed.
        proto (if (= "https:" (.-protocol loc)) "wss" "ws")]
    (str proto "://" (.-host loc) path)))

(defn- websocket-connect [path]
  (js/console.log "websocket-connect")
  (ws/connect (websocket-url path) {:format wsfmt/transit}))

(defn- insert-random-user []
  (go (let [response (<! (http/get "/api/user-random"))
            user (:user (:body response))]
         ;; this is it!
        (set! (.-value (query "#message")) (str "@" user " ")))))



;; from biff,
;; (map message (sort-by :msg/sent-at #(compare %2 %1) messages))
(defn- format-message [{:keys [author message timestamp]}]
  (str "<p>&nbsp" timestamp "<br>&nbsp"
       "<b>" (abbrev author) ":</b> " message "</p>"))

(defn- replace-content [element messages]
  ;; this again. do not forget.
  (set! (.-textContent element) "")
  (doseq [msg (sort-by :timestamp #(compare %1 %2) messages)]
    (.insertAdjacentHTML element "afterbegin" (format-message msg))))

(defn- load-messages [n]
  (go (let [response (<! (http/get (str "/api/load/" n)))
            messages (:body response)]
        (replace-content
         (query "#message-log")
         (remove #(str/starts-with? (:message %) "@") messages)))))

(defn- on-load [_]
  (js/console.log "on-load")
  (go (let [stream  (<! (websocket-connect "/chat"))
            message (query "#message")]
        (start-listener stream (query "#message-log"))
        (.addEventListener (query "#send") "click"
                           (fn [_] (send-message stream)))
        (.focus message)
        (.addEventListener message "keyup"
                           (fn [e]
                             (cond
                               (and (= (.-code e) "Enter") (.-shiftKey e))
                               (send-message stream)
                               (and (= (.-code e) "KeyU") (.-ctrlKey e))
                               (if (admin?)
                                 (insert-random-user)
                                 (js/alert "admin only.")))))
        (.addEventListener (query "#load") "click"
                           (fn [_] (load-messages 10))))))

(defn init []
  (js/console.log "init")
  (.addEventListener js/window "load" on-load))

(ns chat.server
  (:gen-class)
  (:require [buddy.hashers :as hashers]
            [clojure.core.async :as a]
            [clojure.java.io :as io]
            [java-time.api :as jt]
            [clojure.string :as str]
            [hato.client :as hc]
            [muuntaja.middleware :as mw]
            [reitit.ring :as rr]
            [ring.adapter.jetty :as adapter]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.middleware.defaults :as def]
            [ring.util.response :as resp]
            [ring.websocket.transit :as wst]
            [ring.websocket.keepalive :as wska]
            [taoensso.telemere :as t]
            ;; [ring.websocket.async :as wsa]
            ;; patched by hkimura to record chats in database
            [chat.async :as wsa]
            [chat.datascript :as db]))

(def debug? (System/getenv "MX3_DEV"))

(def ^:private version "0.25.0")

; getenv?
(def ^:private l22
  (if debug?
    ""
    "https://l22.melt.kyutech.ac.jp/"))

(defn make-chat-handler []
  (let [writer  (a/chan)
        readers (a/mult writer)]
    (fn handler [_request]
      (wsa/go-websocket [in out]
                        (a/tap readers out)
                        (a/pipe in writer false)))))

(defn login [request]
  (let [flash (:flash request)]
    (-> (resp/response
         (str
          "<!DOCTYPE html><title>Micro X</title>
           <h1>Micro X</h1>
           <body style='font-family:sans-serif;'>
           <form method='post'>"
          (anti-forgery-field)
          (when (some? flash)
            (str "<p style='color:red;'>" flash "</p>"))
          "<input name='login'>
           <input name='password' type='password'>
           <input type='submit' value='LOGIN'>
           <p>version "
          version
          "</p></body></form>"))
        (resp/content-type "text/html")
        (resp/charset "UTF-8"))))

(defn login! [{{:keys [login password]} :params}]
  (t/log! :info (str "login! " login))
  (if debug?
    (-> (resp/redirect "/index")
        (assoc-in [:session :identity] login))
    (try
      (let [resp (hc/get (str l22 "api/user/" login)
                         {:timeout 3000 :as :json})]
        (if (and (some? resp)
                 (hashers/check password (get-in resp [:body :password])))
          (-> (resp/redirect "/index")
              (assoc-in [:session :identity] login))
          (-> (resp/redirect "/")
              (assoc :session {} :flash "login failed"))))
      (catch Exception e
        (t/log! :warn (.getMessage e))
        (-> (resp/redirect "/")
            (assoc :session {} :flash "server does not respond."))))))

(defn index [request]
  (let [login (get-in request [:session :identity] "not-found")]
    (t/log! :info (str "index " login))
    (if (= "not-found" login)
      (-> (resp/redirect "/"))
      (-> (slurp (io/resource "micro-x.html"))
          (str/replace-first #"Anonymous" login)
          resp/response
          (resp/content-type "text/html")
          (resp/charset "UTF-8")))))

(defn- between? [t1 t2 t3]
  (and (neg? (compare t1 t2)) (neg? (compare t2 t3))))

(defn- utime [hhmmss]
  (cond
    (between? "08:50:00" hhmmss "10:20:00") 1
    (between? "10:30:00" hhmmss "12:00:00") 2
    (between? "13:00:00" hhmmss "14:30:00") 3
    (between? "14:40:00" hhmmss "16:10:00") 4
    (between? "16:20:00" hhmmss "17:50:00") 5
    :else 0))

(defn- uhour []
  (let [[wd _ _ hhmmss] (-> (java.util.Date.)
                            str
                            (str/split #"\s"))]
    (if debug?
      "wed1"
      (str/lower-case (str wd (utime hhmmss))))))

(comment
  (uhour)
  (between? "17:00:00" "17:55:02" "18:00:00")
  (compare "17:00:00" "17:30:30")
  (compare "17:30:00" "18:00:00")
  :rcf)

(defn user-random [_]
  (-> (hc/get (str l22 "api/user/" (uhour) "/randomly")
              {:as :json :timeout 1000})
      :body))

;; clojure has a function `load`.
(defn load-records
  "fetch last `n` minutes submissions."
  [n]
  (let [resp (db/q '[:find ?author ?message ?timestamp
                     :keys author message timestamp
                     :in $ ?t0
                     :where
                     [?e :author ?author]
                     [?e :message ?message]
                     [?e :timestamp ?timestamp]
                     [(<= ?t0 ?timestamp)]]
                   (jt/minus (jt/local-date-time) (jt/minutes n)))]
    (t/log! :info (str "load-records " n ":" (first resp) "..."))
    resp))

;; FIXME: want to pass n as `in [n]` and use it with `:limit n`.
;; why not?
; (defn fetch-records
;   "fetch last `n` submissions."
;   [n]
;   (take n
;         (dedupe ; why needed?
;          (db/q '{:find [author message timestamp]
;                  :keys [author message timestamp]
;                  :where [[e :author author]
;                          [e :message message]
;                          [e :timestamp timestamp]]
;                  :order-by [[timestamp :desc]]}))))

(defn make-app-handler []
  (rr/ring-handler
   (rr/router [["/chat" {:middleware [[wst/wrap-websocket-transit]
                                      [wska/wrap-websocket-keepalive]]}
                ["" (make-chat-handler)]]
               ["/api" {:middleware [[def/wrap-defaults def/api-defaults]
                                     mw/wrap-format
                                     mw/wrap-params]}
                ["/load/:n" (fn [{{:keys [n]} :path-params}]
                              (let [n (parse-long n)]
                                (resp/response (load-records n))))]
                ; ["/fetch/:n" (fn [{{:keys [n]} :path-params}]
                ;                (let [n (parse-long n)]
                ;                  (resp/response (fetch-records n))))]
                ["/user-random" {:get (fn [_]
                                        (resp/response (user-random nil)))}]]
               ["" {:middleware [[def/wrap-defaults def/site-defaults]]}
                ["/" {:get login :post login!}]
                ["/logout" (fn [_]
                             (-> (resp/redirect "/")
                                 (assoc :session {})))]
                ["/index" index]]])
   (rr/routes
    (rr/create-resource-handler {:path "/"})
    (rr/create-default-handler))
   {:middleware []}))

;; ----------------------

(defn run-server [options]
  (adapter/run-jetty (make-app-handler) options))

(def server (atom nil))

(defn start
  ([] (if-let [p (System/getenv "PORT")]
        (start {:port (parse-long p)})
        (start {:port 3000})))
  ([{:keys [port]}]
   (t/log! :info "start")
   (when-not (some? @server)
     (reset! server (run-server {:port port :join? false}))
     (db/start! "config.edn")
     (println "server started in port" port))))

(defn stop []
  (when (some? @server)
    (.stop @server)
    (reset! server nil)
    (db/stop!)
    (println "server stopped.")))

(defn restart []
  (stop)
  (start))

(defn -main [& _args]
  (start))


(ns chat.server
  (:gen-class)
  (:require [buddy.hashers :as hashers]
            [clojure.core.async :as a]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [hato.client :as hc]
            [muuntaja.middleware :as mw]
            [reitit.ring :as rr]
            [ring.adapter.jetty :as adapter]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.middleware.defaults :as def]
            [ring.util.response :as resp]
            ;; [ring.websocket.async :as wsa]
            [chat.async :as wsa]
            [ring.websocket.transit :as wst]
            [ring.websocket.keepalive :as wska]
            [taoensso.telemere :as t]
            ;;
            [chat.xtdb :as xt]
            ))
(def debug? (System/getenv "MX3_DEV"))

(def ^:private version "v0.12.116")

(def ^:private l22
  (if debug?
    "http://localhost:3090/"
    "https://l22.melt.kyutech.ac.jp/"))

(t/set-min-level! (if debug? :debug :info))

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
          "<!DOCTYPE html><title>MX3</title>
           <h1>Micro X for Classes</h1>
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
  (if debug?
    (-> (resp/redirect "/index")
        (assoc-in [:session :identity] login))
    (try
      (let [resp (hc/get (str l22 "api/user/"login)
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
    (if (= "not-found" login)
      (-> (resp/redirect "/"))
      (-> (slurp (io/resource "micro-x.html"))
          (str/replace-first #"Anonymous" login)
          resp/response
          (resp/content-type "text/html")
          (resp/charset "UTF-8")))))

;; must be rewritten with java-time. agry.
(defn- utime [t]
  (cond
    debug? "1"
    (< (+ (* 8 60) 50) t  (+ (* 10 60) 20)) "1"
    (< (+ (* 10 60) 30) t (+ (* 12 60))) "2"
    :else "0"))

(defn- uhour []
  (let [[wd _ _ hhmmss] (-> (str (java.util.Date.))
                            (str/split #"\s"))
        [hh mm] (str/split hhmmss #":")
        t (+ (* 60 (Long/parseLong hh)) (Long/parseLong mm))]
    (str/lower-case (str
                     (if debug? "wed" wd)
                     (utime t)))))

(defn user-random [_]
  (-> (hc/get (str l22 "api/user/" (uhour) "/randomly")
              {:as :json :timeout 1000})
      :body))

;; no effect.
;; (defn- wrap-debug [handler]
;;   (fn [request]
;;     (t/log! :debug (:body request))
;;     (handler request)))

(defn make-app-handler []
  (rr/ring-handler
   (rr/router [["/chat" {:middleware [;; wrap-debug
                                      [wst/wrap-websocket-transit]
                                      [wska/wrap-websocket-keepalive]]}
                ["" (make-chat-handler)]]
               ["/api" {:middleware [[def/wrap-defaults def/api-defaults]
                                     mw/wrap-format
                                     mw/wrap-params]}
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

(defn run-server [options]
  (adapter/run-jetty (make-app-handler) options))

(def server (atom nil))

(defn start
  ([] (if-let [p (System/getenv "PORT")]
        (start {:port (Long/parseLong p)})
        (start {:port 8080})))
  ([{:keys [port]}]
   (when-not (some? @server)
     (reset! server (run-server {:port port :join? false}))
     (xt/start! "config.edn")
     (println "server started in port " port "."))))

(defn stop []
  (when (some? @server)
    (.stop @server)
    (reset! server nil)
    (xt/stop!)
    (println "server stopped.")))

(defn restart []
  (stop)
  (start))

(defn -main [& _args]
  (start))

(comment
  (xt/q '{:find [who what when]
          :where [[e :author who]
                  [e :message what]
                  [e :timestamp when]]})
  :rcf)

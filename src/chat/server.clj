(ns chat.server
  (:gen-class)
  (:require [buddy.hashers :as hashers]
            [clojure.core.async :as a]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [hato.client :as hc]
            [reitit.ring :as rr]
            [ring.adapter.jetty :as adapter]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.middleware.defaults :as def]
            [ring.util.response :as resp]
            [ring.websocket.async :as wsa]
            [ring.websocket.transit :as wst]
            [ring.websocket.keepalive :as wska]
            [taoensso.telemere :as t]))

(def ^:private version "v0.10.88")

(def ^:private l22
  (if (System/getenv "MX3_DEV")
    "http://localhost:3090/"
    "https://l22.melt.kyutech.ac.jp/api/user/"))

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
           <h1>Micro X version3</h1>
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
  (if (System/getenv "MX3_DEV")
    (-> (resp/redirect "/index")
        (assoc-in [:session :identity] login))
    (try
      (let [resp (hc/get (str l22 login) {:timeout 3000 :as :json})]
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
    (< (+ (* 8 60) 50) t  (+ (* 10 60) 20)) "1"
    (< (+ (* 10 60) 30) t (+ (* 12 60))) "2"
    :else "0"))

(defn- uhour []
  (let [[wd _ _ hhmmss] (-> (str (java.util.Date.))
                            (str/split #"\s"))
        [hh mm] (str/split hhmmss #":")
        t (+ (* 60 (Long/parseLong hh)) (Long/parseLong mm))]
    (str/lower-case (str
                     (if (System/getenv "MX3_DEV")
                       "wed"
                       wd)
                     (utime t)))))

(defn random-user [_]
  (let [j (hc/get (str l22 "api/user/" (uhour) "/randomly") {:as :json})]
    j))

(comment
  (random-user nil)
  :rcf)

(defn make-app-handler []
  (rr/ring-handler
   (rr/router [["/chat" {:middleware [[wst/wrap-websocket-transit]
                                      [wska/wrap-websocket-keepalive]]}
                ["" (make-chat-handler)]]
               ["/api" {:middleware [[def/wrap-defaults def/api-defaults]]}
                ["/user" {:get (fn [_]
                                 (resp/response (random-user nil))
                                 #_(-> (resp/response {:ret "Hello, World"})))}]]
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
     (println "server started in port " port "."))))

(defn stop []
  (when (some? @server)
    (.stop @server)
    (reset! server nil)
    (println "server stopped.")))

(defn restart []
  (stop)
  (start))

(defn -main [& _args]
  (start))

(comment
  (restart))

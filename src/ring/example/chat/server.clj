(ns ring.example.chat.server
  (:require [buddy.hashers :as hashers]
            [clojure.core.async :as a]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [hato.client :as hc]
            ;; [org.httpkit.client :as hk-client]
            ;; [org.httpkit.server :as hk-server]
            [reitit.ring :as rr]
            [ring.adapter.jetty :as adapter]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.middleware.defaults :as def]
            [ring.util.response :as resp]
            [ring.websocket.async :as wsa]
            [ring.websocket.transit :as wst]
            [ring.websocket.keepalive :as wska]
            [taoensso.telemere :as t]))

(defn make-chat-handler []
  (let [writer  (a/chan)
        readers (a/mult writer)]
    (fn handler [_request]
      (wsa/go-websocket [in out]
                        (a/tap readers out)
                        (a/pipe in writer false)))))

(defn login [_request]
  (-> (resp/response
       (str
        "<!DOCTYPE html><title>MX3</title><h1>Micro X versin 3</h1>"
        "<form method='post'>"
        (anti-forgery-field)
        "<input name='login'>
         <input name='password' type='password'>
         <input type='submit'>
         </form>"))
      (resp/content-type "text/html")
      (resp/charset "UTF-8")))

(def url "https://l22.melt.kyutech.ac.jp/api/user/")

(defn login! [{{:keys [login password]} :params}]
  (let [resp (hc/get (str url login) {:as :json})]
    (if (and (some? resp) (hashers/check password (get-in resp [:body :password])))
      (-> {:status 303
           :headers {"location" "/index"}}
          (assoc-in [:session :identity] login))
      (-> {:status 304
           :headers {"location" "/login"}}
          (assoc :session {} :flash "login failed")))))

(defn index [request]
  (let [login (get-in request [:session :identity] "not-found")]
    (t/log! {:level :info :id "index"} ["identity" login])
    (-> (slurp (io/resource "public/index.html"))
        (str/replace-first #"Anonymous" login)
        resp/response
        (resp/content-type "text/html")
        (resp/charset "UTF-8"))))

(defn make-app-handler []
  (rr/ring-handler
   (rr/router [["/chat" {:middleware [[wst/wrap-websocket-transit]
                                      [wska/wrap-websocket-keepalive]]}
                ["" (make-chat-handler)]]
               ["" {:middleware [[def/wrap-defaults def/site-defaults]]}
                ["/login" {:get login :post login!}]
                ["/index" index]]])
   (rr/routes
    (rr/create-resource-handler {:path "/"})
    (rr/create-default-handler))
   {:middleware []}))

(defn run-server [options]
  (adapter/run-jetty (make-app-handler) options)
  #_(hk-server/run-server (make-app-handler) options))

(def server (atom nil))

(defn start []
  (when-not (some? @server)
    (reset! server (run-server {:port 8080 :join? false}))
    (println "server started in port 8080.")))

(defn stop []
  (when (some? @server)
    (.stop @server)
    #_(@server)
    (reset! server nil)
    (println "server stopped.")))

(defn restart []
  (stop)
  (start))

(comment
  (restart))

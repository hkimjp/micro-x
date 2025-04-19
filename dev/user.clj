(ns user
  (:require [taoensso.telemere :as t]
            [chat.server :as s]
            [hato.client :as hc]))

(t/set-min-level! :debug)

(s/restart)

@s/users
(comment
  (def c (hc/build-http-client {:connect-timeout 100
                                :redirect-policy :always}))

  (hc/get "https://httpbin.org/get" {:http-client c})
  (hc/get "http://localhost:3022/api/user/hkimura" {:http-client c})

  (try
    (hc/get "http://localhost:3022/api/user/himura" {:http-client c})
    (catch Exception e (println (.getMessage e))))

  @s/users
  (s/user-random)
  :rcf)

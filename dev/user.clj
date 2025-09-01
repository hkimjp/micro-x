(ns user
  (:require [taoensso.telemere :as t]
            [chat.server :as s]
            [hkimjp.datascript :as ds]
            #_[hato.client :as hc]))
(comment
  (s/restart)

  @s/users

  (ds/put! {:char "d"})
  (ds/qq '[:find ?e
           :where
           [?e]])
  (ds/pl 2)

  (ds/stop)
  :rcf)

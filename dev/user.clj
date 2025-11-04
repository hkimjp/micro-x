(ns user
  (:require [taoensso.telemere :as t]
            [chat.server :as s]
            [hkimjp.datascript :as ds]))

(t/set-min-level! :debug)
(s/start)

(comment
  (s/restart)
  @s/users
  (ds/put! {:char "d"})
  (ds/qq '[:find ?e
           :where
           [?e]])
  (ds/pl 9)
  (ds/put! {:db/id -1 , :id "check"})
  (ds/pl 10)

  (ds/stop)
  :rcf)

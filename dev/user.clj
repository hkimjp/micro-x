(ns user
  (:require [taoensso.telemere :as t]
            [chat.server :as s]))

(t/set-min-level! :debug)
(s/start)


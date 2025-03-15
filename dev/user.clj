(ns user
  (:require [taoensso.telemere :as t]
            [chat.server :as s]))

(System/getenv "MX3_DEV")

(t/set-min-level! :debug)

(s/restart)


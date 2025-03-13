(ns user
  (:require [taoensso.telemere :as t]
            [chat.server :as s]))

(t/set-min-level! :debug)

(System/getenv "MX3_DEV")

(s/start)


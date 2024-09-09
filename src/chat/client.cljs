(ns chat.client
  (:require [cljs.core.async :as a :refer [<! >! go go-loop]]
            [cljs-http.client :as http]
            [clojure.string :as str]
            [haslett.client :as ws]
            [haslett.format :as wsfmt]))

(defn- query [query]
  (.querySelector js/document query))

(defn author []
  (.-value (query "#author")))

(defn- admin? []
  (= (author) "hkimura"))

(defn- abbrev [s]
  (str (first s) "*****"))

(defn alert [s]
  (.play js/failed)
  (js/alert s))

(defn- append-html [element html]
  ;; https://qiita.com/isseium/items/12b215b6eab26acd2afe
  (.play js/sound)
  (.insertAdjacentHTML element "afterbegin" html))

(defn- message-html [{:keys [author message]}]
  (str "<li><span class='date'>"
       (js/Date.)
       "<br><span class='author'>"
       (if (str/blank? author) "Anonymous" (abbrev author)) "</span>"
       "<span class='message'>" message "</span></li>"))

(defn- empty-message? [s]
  (empty? (-> s
              (str/replace #"^@[^ ]*" "")
              (str/replace #"^\s*" ""))))

(defn- send-message [stream]
  (let [message (query "#message")
        author  (query "#author")]
    (cond (str/starts-with? (.-value message) "＠")
          (alert "全角の ＠ を使っています。")
          (empty-message? (.-value message))
          (alert "メッセージが空(カラ)です．")
          :else (go (>! (:out stream) {:author  (.-value author)
                                       :message (.-value message)})
                    (set! (.-value message) "")
                    (.focus message)))))

(defn- dest [s]
  (re-find #"[^, ]+" (subs s 1)))

(defn- start-listener [stream message-log]
  (go-loop []
    (when-some [message (<! (:in stream))]
      (if (str/starts-with? (:message message) "@")
        (when (= (.-value (query "#author")) (dest (:message message)))
          (append-html message-log (message-html message)))
        (append-html message-log (message-html message)))
      (recur))))

(defn- websocket-url [path]
  (let [loc   (.-location js/window)
        ;; fixed.
        proto (if (= "https:" (.-protocol loc)) "wss" "ws")]
    (str proto "://" (.-host loc) path)))

(defn- websocket-connect [path]
  (ws/connect (websocket-url path) {:format wsfmt/transit}))

(defn- insert-random-user []
  (js/console.log "insert-radom-user")
  (go (let [response (<! (http/get "/api/user-random"))
            user (:user (:body response))]
         ;; this is it!
        (set! (.-value (query "#message")) (str "@" user " ")))))

(defn- deliver-random [stream]
  (go (let [response (<! (http/get "/api/user-random"))
            user (:user (:body response))
            author  (query "#author")
            message (query "#message")]
        (>! (:out stream) {:author (.-value author)
                           :message (str "@" user " " (.-value message))})
        (set! (.-value message) "")
        (.focus message))))

;; from biff,
;; (map message (sort-by :msg/sent-at #(compare %2 %1) messages))
(defn- format-message [{:keys [author message timestamp]}]
  (str "<p>&nbsp" timestamp "<br>&nbsp"
       "<b>" (abbrev author) ":</b> " message "</p>"))

(defn- replace-content [element messages]
  ;; this again. do not forget.
  (set! (.-textContent element) "")
  (doseq [msg (sort-by :timestamp #(compare %1 %2) messages)]
    (.insertAdjacentHTML element "afterbegin" (format-message msg))))

(defn- remove? [{:keys [author message]}]
  (let [owner (.-value (query "#author"))]
    (if (= author owner)
      false
      (if (str/starts-with? message (str "@" owner " "))
        false
        (str/starts-with? message "@")))))

(defn- load-messages [n]
  (go (let [response (<! (http/get (str "/api/load/" n)))
            messages (:body response)]
        (replace-content
         (query "#message-log") (remove remove? messages)))))

(defn- on-load [_]
  (go (let [stream  (<! (websocket-connect "/chat"))
            message (query "#message")]
        (start-listener stream (query "#message-log"))
        (.addEventListener (query "#send") "click"
                           (fn [_] (send-message stream)))
        (.focus message)
        (.addEventListener message "keyup"
                           (fn [e]
                             (cond
                               (and (= (.-code e) "Enter") (.-shiftKey e))
                               (send-message stream)
                               (and (= (.-code e) "KeyU") (.-ctrlKey e))
                               (if (admin?)
                                 (insert-random-user)
                                 (alert "^U admin only."))
                               (and (= (.-code e) "KeyI") (.-ctrlKey e))
                               (if (admin?)
                                 (deliver-random stream)
                                 (alert "^I admin only.")))))
        (.addEventListener (query "#load") "click"
                           (fn [_] (load-messages 10))))))

(defn init []
  (.addEventListener js/window "load" on-load))

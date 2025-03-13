(ns chat.datascript
  (:require
   [clojure.java.io :as io]
   [clojure.main :as main]
   ;; [clojure.pprint :refer [pprint]]
   [datascript.core :as d]
   [datascript.storage.sql.core :as storage-sql]
   [taoensso.telemere :as t]
   ;;[xtdb.api :as xt]
   ))

(def db "target/db.sqlite")

(defn make-storage [db]
  (let [datasource (doto (org.sqlite.SQLiteDataSource.)
                     (.setUrl (str "jdbc:sqlite:" db)))
        pooled-datasource (storage-sql/pool
                           datasource
                           {:max-conn 10
                            :max-idle-conn 4})]
    (storage-sql/make pooled-datasource {:dbtype :sqlite})))

(def storage (make-storage db))

(def conn nil)

(defn create []
  (t/log! :info "create storage backend datascript.")
  (def strogage (make-storage db))
  (def conn (d/create-conn {:storage storage})))

(defn restore []
  (t/log! :info "restore")
  (def conn (d/restore-conn storage)))

(defn start! [_]
  (t/log! :info "start on-memory database")
  (def conn (d/create-conn)))

(defn stop! []
  (t/log! :info "stop!")
  (storage-sql/close storage)
  (def conn nil))

(defmacro q [query & inputs]
  (t/log! :info (str "q " query))
  `(d/q ~query @conn ~@inputs))

(defn put! [fact]
  (t/log! :info (str "put! " fact))
  (d/transact! conn [fact]))

(defn puts! [facts]
  (t/log! :info (str "puts! " facts))
  (d/transact! conn facts))

(defn pull
  ([eid] (pull ['*] eid))
  ([selector eid] (t/log! :info (str "pull " selector " " eid))
                  (d/pull @conn selector eid)))

(comment
  (q '[:find ?e ?time
       :where
       [?e :timestamp ?time]])
  :rcf)

; (defn start!
;   ([]
;    (if (status?)
;      (println "already started.")
;      (reset! node (xt/start-node {}))))
;   ([fname]
;    (if (status?)
;      (println "already started.")
;      (reset! node (xt/start-node (read-config fname))))))

; (defn stop! []
;   (when (status?)
;     (.close @node)
;     (reset! node nil)))

; (defn put!
;   "puts a doc then sync."
;   [doc]
;   (xt/submit-tx @node [[::xt/put doc]])
;   (xt/sync @node))

; (defn puts! [docs]
;   (xt/submit-tx @node (for [doc docs]
;                         [::xt/put doc]))
;   (xt/sync @node))

; (defmacro q [query & opt]
;   `(xt/q (xt/db @node) ~query ~@opt))

;; FIXME: define pull?
; (defn client [{:keys [config]}]
;   (println "config read" config)
;   (println "do (in-ns 'chat.xtdb)")
;   (start! config)
;   (main/repl))

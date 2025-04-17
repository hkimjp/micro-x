(ns chat.datascript
  (:require
   ; [clojure.java.io :as io]
   ; [clojure.main :as main]
   [datascript.core :as d]
   [datascript.storage.sql.core :as storage-sql]
   [taoensso.telemere :as t]))

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

; FIXME: inline def
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


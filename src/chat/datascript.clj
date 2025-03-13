(ns chat.datascript
  (:require
   [clojure.java.io :as io]
   [clojure.main :as main]
   ;; [clojure.pprint :refer [pprint]]
   [datascript.core :as d]
   [datascript.storage.sql.core :as storage-sql]
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

#_(defn kv-store [dir]
    {:kv-store {:xtdb/module 'xtdb.rocksdb/->kv-store
                :db-dir (io/file dir)
                :sync? true}})

#_(defn read-config
    [fname]
    (let [conf (read-string (slurp (io/file fname)))
          ret {:xtdb/tx-log         (kv-store (:tx-log conf))
               :xtdb/index-store    (kv-store (:index-store conf))
               :xtdb/document-store (kv-store (:document-store conf))}]
      #_(if-let [port (:port conf)]
          (assoc ret :xtdb.http-server/server {:port port})
          ret)
      ret))

#_(defn status? []
    (some? @node))

(defn start!
  ([]
   (if (status?)
     (println "already started.")
     (reset! node (xt/start-node {}))))
  ([fname]
   (if (status?)
     (println "already started.")
     (reset! node (xt/start-node (read-config fname))))))

(defn stop! []
  (when (status?)
    (.close @node)
    (reset! node nil)))

(defn put!
  "puts a doc then sync."
  [doc]
  (xt/submit-tx @node [[::xt/put doc]])
  (xt/sync @node))

(defn puts! [docs]
  (xt/submit-tx @node (for [doc docs]
                        [::xt/put doc]))
  (xt/sync @node))

(defmacro q [query & opt]
  `(xt/q (xt/db @node) ~query ~@opt))

;; FIXME: define pull?

(defn client [{:keys [config]}]
  (println "config read" config)
  (println "do (in-ns 'chat.xtdb)")
  (start! config)
  (main/repl))

(comment
  (start!)
  (put! {:xt/id 0
         :greeting "hello"
         :to "world"})
  (stop!)
  :rcf)

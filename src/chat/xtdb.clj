(ns chat.xtdb
  (:require
   [clojure.java.io :as io]
   [xtdb.api :as xt]))

(def node (atom nil))

(defn kv-store [dir]
  {:kv-store {:xtdb/module 'xtdb.rocksdb/->kv-store
              :db-dir (io/file dir)
              :sync? true}})

(defn read-config
  [fname]
  (let [conf (read-string (slurp (io/file (io/resource fname))))
        ret {:xtdb/tx-log         (kv-store (:tx-log conf))
             :xtdb/index-store    (kv-store (:index-store conf))
             :xtdb/document-store (kv-store (:document-store conf))}]
    #_(if-let [port (:port conf)]
      (assoc ret :xtdb.http-server/server {:port port})
      ret)
    ret))

(defn status? []
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

;; pull

(comment
  (start!)
  (put! {:xt/id 0
         :greeting "hello"
         :to "world"})
  (stop!)
  :rcf)

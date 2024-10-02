(ns build
  (:refer-clojure :exclude [test])
  (:require [clojure.tools.build.api :as b]))

(def lib 'build/micro-x)
(def version "v0.20-SNAPSHOT")
(def main 'chat.server)
(def class-dir "target/classes")

(defn- uber-opts [opts]
  (assoc opts
         :lib lib :main main
         :uber-file (format "target/%s-%s.jar" lib version)
         :basis (b/create-basis {})
         :class-dir class-dir
         :src-dirs ["src"]
         :ns-compile [main]))

(defn uber [opts]
  (b/delete {:path "target"})
  (let [opts (uber-opts opts)]
    (println (str "\nCompiling client ..."))
    (b/process {:command-args ["clojure" "-M:cljs" "release" "client"]})
    (println "\nCopying source...")
    (b/copy-dir {:src-dirs ["resources" "src"] :target-dir class-dir})
    (println (str "\nCompiling " main "..."))
    (b/compile-clj opts)
    (println "\nBuilding JAR...")
    (b/uber opts))
  opts)

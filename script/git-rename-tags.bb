#!/usr/bin/env bb

(require '[babashka.process :refer [shell]])

(def tags (->> (shell/sh "git" "tag")
               :out
               str/split-lines
               (filter #(str/starts-with? % "v"))))

(doseq [old tags]
  (println old)
  (shell/sh "git" "tag" (subs old 1) old)
  (shell/sh "git" "tag" "-d" old)
  (shell/sh "git" "push" "origin" (str ":" old)))

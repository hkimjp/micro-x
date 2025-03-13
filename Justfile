client:
	clj -M:cljs compile client

watch:
	clj -M:cljs watch client &

dev: watch
    clojure -M:dev -m nrepl.cmdline


client:
	clj -M:cljs compile client

dev: client
    clojure -M:dev -m nrepl.cmdline

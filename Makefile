client:
	clj -M:cljs compile client

start:
	MX3_DEBUG=1 clojure -X:server &

kill:
	killp 8080

restart:
	make kill
	make start

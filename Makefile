client:
	clj -M:cljs compile client

start:
	MX3_DEBUG=1 clojure -X:server &

kill:
	killp 8080

restart:
	make kill
	make start

build:
	rm -rf target
	make client
	clj -T:build uber

debug:
	(cd target/build && MX3_DEBUG=1 java -jar micro-x-*.jar)

jammy:
	scp target/build/micro-x-*.jar jammy.local:micro-x/

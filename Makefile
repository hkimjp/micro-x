PORT=8080

client:
	clj -M:cljs compile client

develop:
	MX3_DEV=1 clj -X:server :port ${PORT}

start:
	clj -X:server :port ${PORT}

stop:
	kill `lsof -t -i:${PORT}`

restart:
	make stop
	make start

build:
	rm -rf target
	make client
	clj -T:build uber

jammy:
	scp target/build/micro-x-*.jar jammy.local:micro-x/

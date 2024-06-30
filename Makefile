PORT=8080

client:
	clj -M:cljs compile client

develop:
	MX3_DEV=1 clj -X:server :port ${PORT}

start:
	clj -X:start :port ${PORT}

stop:
	kill `lsof -t -i:${PORT}`

restart:
	make stop
	make start

build:
	clj -T:build uber

deploy: build
	scp target/build/micro-x-*.jar app.melt:micro-x/micro-x.jar
	ssh app.melt 'sudo systemctl restart micro-x.service'

jammy:
	scp target/build/micro-x-*.jar jammy.local:micro-x/micro-x.jar

clean:
	${RM} -r target resources/public/js

realclean:
	make clean
	${RM} -r .clj-kondo .lsp .shadow-cljs

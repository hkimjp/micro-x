PORT=8080

client:
	clj -M:cljs compile client

watch:
	clj -M:cljs watch client

develop:
	MX3_DEV=1 clj -X:start :port ${PORT}

start:
	clj -X:start :port ${PORT}

stop:
	kill `lsof -t -i:${PORT}`

restart:
	make stop
	make start

build:
	# app.melt's jdk is `openjdk 11.0.23 2024-04-16`.
	# JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.12/libexec/openjdk.jdk/Contents/Home \
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

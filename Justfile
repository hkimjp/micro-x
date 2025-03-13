client:
	clj -M:cljs compile client

watch:
	clj -M:cljs watch client &

dev: watch
    clojure -M:dev -m nrepl.cmdline

build:
	clj -T:build uber

SERV := 'ubuntu@app.melt.kyutech.ac.jp'
DEST := 'ubuntu@app.melt.kyutech.ac.jp:micro-x'
deploy: build
	scp target/build/micro-x-*.jar {{DEST}}/micro-x.jar
	ssh {{SERV}} sudo systemctl restart micro-x

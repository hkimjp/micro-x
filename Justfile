set dotenv-load

run:
  clojure -M:cljs compile client
  if [ ! -d "target" ]; then \
    mkdir target; \
  fi
  clojure -X:server

watch:
  clojure -M:cljs watch client

repl:
  if [ ! -d "target" ]; then \
    mkdir target; \
  fi
  MX3_DEV=true clojure -M:dev -m nrepl.cmdline

stop:
  kill `lsof -t -i:${PORT}`

# FIXME: restart cljs?
restart:
  just stop
  just start

build:
  clojure -T:build uber

deploy: build
  scp target/hkimjp/micro-x-*.jar ${DEST}/micro-x.jar
  ssh ${SERV} sudo systemctl daemon-reload
  ssh ${SERV} sudo systemctl restart micro-x

clean:
  rm -rf target

set dotenv-load

run: compile
  if [ ! -d "storage" ]; then \
    mkdir storage; \
  fi
  clojure -X:server

compile:
  clojure -M:cljs compile client

watch:
  clojure -M:cljs watch client

repl:
  if [ ! -d "storage" ]; then \
    mkdir storage; \
  fi
  clojure -M:dev -m nrepl.cmdline

stop:
  kill `lsof -t -i:${PORT}`

# FIXME: restart cljs?
# restart:
#   just stop
#   just repl

upgrade:
  clj -Tantq outdated :upgrade true

build:
  clojure -T:build uber

deploy: compile build
  scp target/io.github.hkimjp/micro-x-*.jar ${DEST}/micro-x.jar
  ssh ${SERV} sudo systemctl daemon-reload
  ssh ${SERV} sudo systemctl restart micro-x
  ssh ${SERV} systemctl status micro-x


clean:
  rm -rf target


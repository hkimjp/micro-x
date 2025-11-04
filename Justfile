set dotenv-load

run: compile
  if [ ! -d "storage" ]; then \
    mkdir storage; \
  fi
  clojure -X:server

compile:
  clojure -J--enable-native-access=ALL-UNNAMED -M:cljs compile client

release:
  clojure -J--enable-native-access=ALL-UNNAMED -M:cljs release client

watch:
  clojure -J--enable-native-access=ALL-UNNAMED -M:cljs watch client

nrepl:
  clojure -M:dev -m nrepl.cmdline

dev:
  just watch &
  just nrepl

stop:
  kill `lsof -t -i:${PORT}`

upgrade:
  clj -Tantq outdated :upgrade true

build:
  clojure -T:build uber

deploy: release build
  scp target/io.github.hkimjp/micro-x-*.jar ${DEST}/micro-x.jar
  ssh ${SERV} sudo systemctl daemon-reload
  ssh ${SERV} sudo systemctl restart micro-x
  ssh ${SERV} systemctl status micro-x

clean:
  rm -rf target

eq: release build
  scp target/io.github.hkimjp/micro-x-*.jar eq.local:micro-x/micro-x.jar
  scp .env start.sh eq.local:micro-x/
  ssh eq.local 'cd micro-x && ./start.sh > log/micro-x.log 2>&1'

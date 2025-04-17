set dotenv-load

client:
  clj -M:cljs compile client

server:
  if [ ! -d "target" ]; then \
    mkdir target; \
  fi
  clj -X:server

watch:
  clj -M:cljs watch client

dev:
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
  clj -T:build uber

SERV := 'ubuntu@app.melt.kyutech.ac.jp'
DEST := 'ubuntu@app.melt.kyutech.ac.jp:micro-x'

deploy: build
  scp target/build/micro-x-*.jar {{DEST}}/micro-x.jar
  ssh {{SERV}} sudo systemctl restart micro-x

clean:
  rm -rf target

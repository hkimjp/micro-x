# simulated Micro X for hkimura Classes

## Unreleased

* who is login now?
  => redis. when logout, expire. or exire after a period.
* display error message when sent to non-exist users.
* cache messages with `redis`.
* ポップアップメニュー、キーバインドを表示する。
* msg に通算の id
* /api/users を用意、on-load 時に初期化して使い回す。


## 0.25.3-SNAPSHOT


## 0.25.2 (2024-04-18)

* .env のコピーを忘れた。旧ワークスペースからコピー。
* 新しい .git で just deploy できることを確認した。
* リモートリポジトリ引越し
* added script/git-rename-tags.bb
* removed src/chat/datascript.clj. changed to hkimjp/util/datascript.
* upgraded hkimjp/util "0.2.4".
* libraries update, use hkimjp/util

| :file    | :name                         | :current | :latest |
|--------- | ----------------------------- | -------- | --------|
| deps.edn | io.github.clojure/tools.build | v0.10.7  | v0.10.8 |
|          | io.github.hkimjp/util         | 0.2.0    | 0.2.4   |
|          | metosin/reitit                | 0.7.2    | 0.8.0   |
|          | ring/ring-core                | 1.13.0   | 1.14.1  |
|          | ring/ring-jetty-adapter       | 1.13.0   | 1.14.1  |
|          | thheller/shadow-cljs          | 2.28.21  | 2.28.23 |

## 0.25.0 (2025-04-18)

* ログをとる。
* (re)load で表示するメッセージを1日分(24 x 60min)にした。
* fixed: Justfile
* removed: Makefile
* changed Justfile: client, server をまとめて run とした。
* changed log level: chat.datascrit, from :info to :debug
* added `mins-to-load` in `client.cljs`: load messages last `mins-to-load` minutes
* ログアウトは on-close イベントが届く。

## 0.24.0 (2025-03-15)

* Justfile: set dotenv-load
* .env gitignored
* target/.keep
* rearrange the new messages at the bottom.
* changed the policy of version tags.

## v0.23.255 / 2025-03-13

* .env, gitignored.
* MX3_DEV=1 で l22 を見に行かない。


## v0.22.251 / 2025-03-13

* can load (debugged).
* Just receipe `watch`.
* Just receipe `dev` to call `watch`.
* added `dev/user.clj`.

## v0.21.248 / 2025-03-13

* added `Justfile`.
* updated `async.clj` for datascript.
* [chat.xtdb :as db]
* added `src/chat/datascript.clj`.
* updated libraries,

| :file    | :name                                      | :current  | :latest   |
|--------- | ------------------------------------------ | --------- | ----------|
| deps.edn | clojure.java-time/clojure.java-time        | 1.4.2     | 1.4.3     |
|          | com.taoensso/telemere                      | 1.0.0-RC4 | 1.0.0-RC5 |
|          | com.xtdb/xtdb-core                         | 1.24.4    | 2.0.0-b1  |
|          | io.github.clojure/tools.build              | v0.10.5   | v0.10.7   |
|          | metosin/muuntaja                           | 0.6.10    | 0.6.11    |
|          | org.ring-clojure/ring-websocket-middleware | 0.2.0     | 0.2.1     |
|          | org.xerial/sqlite-jdbc                     | 3.48.0.0  | 3.49.1.0  |
|          | ring/ring-anti-forgery                     | 1.3.1     | 1.4.0     |
|          | ring/ring-core                             | 1.12.2    | 1.13.0    |
|          | ring/ring-defaults                         | 0.5.0     | 0.6.0     |
|          | ring/ring-jetty-adapter                    | 1.12.2    | 1.13.0    |
|          | thheller/shadow-cljs                       | 2.28.15   | 2.28.21   |

## v0.20.240 / 2025-01-15

- viewport.
- chat.client/deliver-random: not clear input `message` after sending.
  to send the same message repeatedly.

## v0.19.227 / 2024-09-29

- solved: can not `gh repo clone`.
  use class GITHUB_API_TOKEN instead of find-grained GH_TOKEN.
- use `clojure.core/parse-long` instead of `Long/parseLong`.
  (from clojure 1.11).
- redefined `between?` using `compare`.


## v0.18.218 / 2024-09-09

- take care `go loop`
- libraries updated

| :file    | :name                         | :current     | :latest      |
|--------- | ----------------------------- | ------------ | -------------|
| deps.edn | com.taoensso/telemere         | 1.0.0-beta14 | 1.0.0-beta22 |
|          | hato/hato                     | 0.9.0        | 1.0.0        |
|          | io.github.clojure/tools.build | v0.9.2       | v0.10.5      |
|          | metosin/reitit                | 0.7.0        | 0.7.2        |
|          | org.clojure/clojure           | 1.11.3       | 1.12.0       |
|          | thheller/shadow-cljs          | 2.28.10      | 2.28.14      |


## v0.18.211 / 2024-07-11

- changed: cntrl+I delivers secretly to a random user.

## v0.17.203 / 2024-07-05

- fake argument to main.js. effects?
```
main.js?v=version-number
```

## v0.17.199 / 2024-07-05
- alert empty messages.
```clj
(defn- empty-message? [s]
  (-> s
      (str/replace #"^@[^ ]*" "")
      (str/replace #"^\s*" "")
      empty?))
```

## v0.17.190 / 2024-07-05
- load-records and fetch-records.
- the function names do not reflect their functions.
- fixme: order-by and in [n].

## v0.16.179 / 2024-06-30
- add alert(s)

## v0.16.173
- load displays message directed to me.
- can not filter from messages to me by sender.
  because messages does not have from information. change message format?
- added chat.xtdb/client
- messages whose owner is self are always `load`.

## v0.15.163 / 2024-06-30
- `maou_se_system28.mp3` by Pantsman.

## v0.15.159 / 2024-06-30
- hide @who when `load`ed.
- fixed: if start by `clj -X:server`, xtdb is not started.
  use `make start`.
- chat.client/load-messages
```clj
(remove #(str/starts-with? (:message %) "@") messages)
```

## v0.14.152 / 2024-06-30
- add sound.

## v0.13.141 / 2024-06-29
- (abbrev author) - - do not dislay user names.
- load. last 10 or last 10 minutes messages.

## v0.13.136 / 2024-06-29
- format-message

## v0.13.134 / 2024-06-29
- added a load button.
- added a route '/api/load'.
- added a client function, client/load-messages
- added an incomplete function, client/replace-content
- learn 'pull'. pull returns #{[] [] ...}. It is not good.
- sort by timestamp on client.clj.

## v0.12.116 / 2024-06-28
- only admin can use Ctrl+U.
- if jetty lives, xtdb-http can not?
- added `async.clj` based on `ring.websocket.async`.

## v0.11.107 / 2024-06-28
- tag?

## v0.11.99 / 2024-06-28
- not random button, assign key.
- changed my mind. cljs-http/cljs-http {:mvn/version "0.1.48"}
- ctrl+U put '@user ' to '#message'.
```
  (set! (.-value (query "#message")) (str "@" user " "))
```

## v0.11.97
- both function and endpoint are 'user-random'.

## v0.11.96 / 2024-06-28
- added /api/user-random - returns {:user "login"}

## v0.10.88 / 2024-06-27
- systemd service.
- update "Makefile".

## v0.9.83 / 2024-06-27
- "make deploy".
- cleanup "Makefile".
- found how to compile by `clj -T:build uber`.
```
  (b/process {:command-args ["clojure" "-M:cljs" "compile" "client"]})
```
- changed - MX3_DEBUG -> MX3_DEV.

## v0.8.75 / 2024-06-26
- bump-version.sh bumps `build.clj`.
- warn full-width at-mark.

## v0.8.64 / 2024-06-26
- successed `make build`.
- deps.edn
- build.clj
- shadow-cljs.edn

## v0.7.58 / 2024-06-26
- insert new message after begin.
- displayed failed login in red, use flash.
- updated `Makefile`.
- assured `favicon.ico`.

## v0.7.52 / 2024-06-25
code cleanup.
- shift+Enter sends a message.
- ring.util.response intead of {:status 303 :headers {"location" ...}}.
- hato timeout 3000msec.
- MX3\_DEBUG
```clojure
(if (System/getenv "MX3_DEBUG")
    (-> (resp/redirect "/index")
        (assoc-in [:session :identity] login)))
```

## v0.6.40 / 2024-06-24
- fixed nginx can transfer websocket data.
```clojure
(defn- websocket-url [path]
  (js/console.log "websocket-url")
  (let [loc   (.-location js/window)
        ;; fixed. was "http".
        proto (if (= "https:" (.-protocol loc)) "wss" "ws")]
    (str proto "://" (.-host loc) path)))
```

## v0.6.1 / 2024-06-24
- bump-version.sh updates chat/server.clj `version`.

## v0.5.30 / 2024-06-24
- unicast emulation.
- namespace.

## v0.4.22 / 2024-06-24
- restriction inside index.
- logout. `#(assoc % session {})`.
- moved `/resources/public/index.html` to `/resources/index.html`.
- redirect / to /login? -> moved `/login` as `/`.

## v0.3.18 / 2024-06-24
- auth.
- flash.
- hk-server is not good against ring-example server.
  differs websocket API between jetty and http-kit?
- hk-client can not parse (get-in resp [:body :password])
  need cachire to decode? (hc/get url {:as :json}) is fine.

## v0.2.12 / 2024-06-24
- moved default middleware to "/chat".
- added "/login".

## v0.1.0 / 2024-06-24
- initialized repository.
- updated libraries.
- it works.
```shell
% clj -M:cljs compile client
% clj -X:server
```

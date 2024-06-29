# Micro X for Hkimura Classes

## Unreleased
- who is login now?
- do not dislay user login in chat. instead, clock.
- sound.
- load. last 10 or last 10 minutes messages.
- display error message when sent to a non-existent user.
- abbdev sender in 'Load'.

## v0.13-SNAPSHOT
- (abbrev author)

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
- both function and endpoint is 'user-random'.

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
  differ websocket API, jetty and http-kit?
- hk-client, too. need cachire to decode? (hc/get url {:as :json}) is fine.

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

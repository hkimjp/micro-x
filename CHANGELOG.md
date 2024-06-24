# Micro X version 3

## Unreleased
- sound.
- it is bad to send message when Enter key?
- build.
- nginx can not transfer websocket data.


## v0.5.30 / 2024-06-24
- unicast emulation.

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
```
% clj -M:cljs compile client
% clj -X:server
```

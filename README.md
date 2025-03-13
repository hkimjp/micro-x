# Simulated Micro X based on Ring WebSocket Chat Example

Provide @user facility which enables (simulated) unicast messaging.

For development,

```
% make client
% MX3_DEV=1 make start
```

or simply,

```
% make develop
```

## Require

- openjdk >= 21
- clojure >= 1.11
  clojure.core/parse-long appears in clojure 1.11.

## Usage

* Shift + Enter  send message
* Cntl + U       insert random user
* Cntl + I       deliver message to random users

## solved

* can not gh repo clone but can `git clone`. Fine-grained GH\_TOKEN is not good yet. Use classic GITHUB\_API\_TOKEN.

---

## original documents

This is an example project that demonstrates how to use WebSockets in
[Ring][] to create a simple chat web application.

This project uses [Reitit][] for routing, and [Haslett][] to handle
WebSockets client-side. Building the application is handled via
[tools.deps], and compiling the ClojureScript is managed by
[shadow-cljs].

To run it, first compile the ClojureScript using:

    clj -M:cljs compile client

Then start the server with:

    clj -X:server

By default, the server can be accessed at: <http://localhost:8080>

[Ring]: https://github.com/ring-clojure/ring
[Reitit]: https://github.com/metosin/reitit
[Haslett]: https://github.com/weavejester/haslett
[tools.deps]: https://github.com/clojure/tools.deps
[shadow-cljs]: https://github.com/thheller/shadow-cljs

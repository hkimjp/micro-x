#!/bin/sh

if [ -z "$1" ]; then
    echo "usage: $0 <version>"
    exit
fi

if [ -x "${HOMEBREW_PREFIX}/bin/gsed" ]; then
    SED="${HOMEBREW_PREFIX}/bin/gsed -E"
else
    SED="/usr/bin/sed -E"
fi

# clj
${SED} -i "s/(def \^:private version) .+/\1 \"$1\")/" src/chat/server.clj

# micro-x.html
${SED} -i "s/(main.js\?v=).*\"/\1$1\")/" resources/micro-x.html

# server.clj
${SED} -i -e "/\(def version/c\
(def version \"${VER}\")" src/chat/server.clj

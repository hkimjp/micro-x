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

# CHANGELOG.md
VER=$1
TODAY=`date +%F`
${SED} -i -e "/SNAPSHOT/c\
## ${VER} / ${TODAY}" CHANGELOG.md

# clj
${SED} -i "s/(def \^:private version) .+/\1 \"$1\")/" src/chat/server.clj

# build.clj
${SED} -i "s/(def version) .+/\1 \"$1\")/" build.clj

# micro-x.html
${SED} -i "s/(main.js\?v=).*\"/\1$1\")/" resources/micro-x.html

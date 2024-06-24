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
${SED} -i.bak -e "/SNAPSHOT/c\
## ${VER} / ${TODAY}" CHANGELOG.md

# clj
#${SED} -i "s/(def \^:private version) .+/\1 \"$1\")/" src/server.clj

# cljs
#${SED} -i "s/(def \^:private version) .+/\1 \"$1\")/" src/client.cljs

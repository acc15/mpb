#!/bin/sh

CP="${CP:-"$(pwd)/dist"}/*"
NOBUILD="${NOBUILD:-false}"

if [[ $NOBUILD != "true" ]]; then
    gradle dist -P dir=dist || exit 1
fi

OPTS=(-cp "$CP" ru.vm.mpb.MainKt)
if [[ $DEBUG == ?(-)+([[:digit:]]) ]]; then
	OPTS=("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:$DEBUG" "${OPTS[@]}")
fi

cd "$HOME/MyProgs/it-one/epgu/scripts/mpb"
java "${OPTS[@]}" "$@"

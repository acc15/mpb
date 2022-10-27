#!/bin/sh

CP="$(pwd)/dist/*"
gradle dist -P dir=dist

OPTS=(-cp "$CP" ru.vm.mpb.MainKt)
if [[ $DEBUG == 'y' ]]; then
	OPTS=("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005" "${OPTS[@]}")
fi

cd "$HOME/MyProgs/it-one/epgu/scripts/mpb"
java "${OPTS[@]}" "$@"

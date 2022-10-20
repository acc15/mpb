function mpb() {

	local JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk}"
	local JAVA="$JAVA_HOME/bin/java"
	local MPB_HOME="$HOME/.local/lib/mpb"
	local MPB_MAIN="ru.vm.mpb.MainKt"
	local CD_FILE='/tmp/mpb_cd.txt'
  local SCRIPT_HOME="" # Put script home here

	(cd "$SCRIPT_HOME" && "$JAVA" -cp "$MPB_HOME/*" "$MPB_MAIN" "$@")
	if [[ -f "$CD_FILE" ]]; then
		cd $(cat "$CD_FILE")
		rm "$CD_FILE"
	fi

}

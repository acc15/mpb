function mpb() {

	JAVA_HOME="/usr/lib/jvm/java-17-openjdk"
	JAVA="$JAVA_HOME/bin/java"
	MPB_HOME="$HOME/.local/lib/mpb"
	MPB_MAIN="ru.vm.mpb.MainKt"
	SCRIPT_HOME="$HOME/MyProgs/it-one/epgu/scripts/mpb"
	CD_FILE='/tmp/mpb_cd.txt'

	(cd $SCRIPT_HOME && "$JAVA" -cp "$MPB_HOME/*" "$MPB_MAIN" "$@")
	if [[ -f $CD_FILE ]]; then
		cd $(cat $CD_FILE)
		rm $CD_FILE
	fi

}

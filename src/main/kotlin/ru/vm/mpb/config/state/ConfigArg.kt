package ru.vm.mpb.config.state

object ConfigArg {

    fun parse(vararg args: String): Config {

        val optPrefix = "--"
        val defaultPath = ConfigPath.parse("args")
        val state = Config.immutable(mutableMapOf<String, Any>())

        val values = mutableListOf<String>()
        var path = defaultPath

        fun flush() {
            if (values.isEmpty() && path != defaultPath) {
                state.path(path).set(true)
            }
            for (v in values) {
                state.path(path).add(v)
            }
            values.clear()
        }

        for (a in args) {
            if (a.startsWith(optPrefix)) {
                flush()
                path = ConfigPath.parse(a.substring(optPrefix.length)).ifEmpty { defaultPath }
                continue
            }
            values.add(a)
        }
        flush()
        return state
    }

}
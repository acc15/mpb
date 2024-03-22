package ru.vm.mpb.config.state

object ConfigArg {

    private const val LONG_OPT_PREFIX = "--"
    private const val SHORT_OPT_PREFIX = "-"

    fun parse(vararg args: String): Config {

        val defaultPath = ConfigPath.parse("args")
        val state = ConfigRoot()

        val values = mutableListOf<String>()
        var path = defaultPath

        fun flush() {
            if (values.isEmpty() && path != defaultPath) {
                state.path(path).set(true)
            } else {
                values.forEach { state.path(path).add(it) }
            }
            values.clear()
            path = defaultPath
        }

        for (a in args) {
            if (a.startsWith(LONG_OPT_PREFIX)) {
                flush()
                val optPath = a.substring(LONG_OPT_PREFIX.length)
                path = ConfigPath.parse(optPath).ifEmpty { defaultPath }
            } else if (a.startsWith(SHORT_OPT_PREFIX)) {
                flush()
                val opt = a.substring(SHORT_OPT_PREFIX.length)
                val keyValue = opt.split('=', limit = 2)
                path = ConfigPath.parse(keyValue[0]).ifEmpty { defaultPath }
                values.addAll(keyValue.getOrNull(1)?.split(",") ?: emptyList())
                flush()
            } else {
                values.add(a)
            }
        }
        flush()
        return state
    }

}
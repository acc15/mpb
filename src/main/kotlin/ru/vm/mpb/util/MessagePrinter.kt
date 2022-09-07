package ru.vm.mpb.util

import ru.vm.mpb.config.MpbConfig
import java.io.PrintStream

class MessagePrinter(
    private val cfg: MpbConfig,
    private val prefix: String? = null,
    private val ps: PrintStream = System.out
) {
    fun print(str: Any?, e: Throwable? = null, prefix: String? = null) {
        val p = prefix ?: this.prefix
        ps.println(if (p != null) "[$p] $str" else str)
        if (cfg.debug && e != null) {
            e.printStackTrace(ps)
        }
    }
}

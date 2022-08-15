package ru.vm.mpb.util

import ru.vm.mpb.config.MpbConfig
import java.io.PrintStream

class MessagePrinter(val cfg: MpbConfig, val prefix: String? = null, val ps: PrintStream = System.out) {

    operator fun invoke(str: Any?, e: Throwable? = null) {
        if (prefix != null) {
            ps.println("[$prefix] $str")
        } else {
            ps.println(str)
        }
        if (cfg.debug && e != null) {
            e.printStackTrace(ps)
        }
    }

    fun withPrefix(other: String) = MessagePrinter(cfg, other, ps)

}

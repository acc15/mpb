package ru.vm.mpb.printer

import ru.vm.mpb.config.MpbConfig
import java.io.PrintStream

class DefaultPrinter(val out: PrintStream, val cfg: MpbConfig): Printer {
    override suspend fun print(data: PrintData) {
        out.println(if (data.key.isEmpty()) "${data.msg}" else "[${data.key}] ${data.msg}")
        if (cfg.debug && data.ex != null) {
            data.ex.printStackTrace(out)
        }
    }

    override fun close() {
    }
}
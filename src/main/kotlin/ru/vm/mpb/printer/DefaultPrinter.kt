package ru.vm.mpb.printer

import ru.vm.mpb.config.OutputConfig
import java.io.PrintStream

class DefaultPrinter(val out: PrintStream, val cfg: OutputConfig): Printer {
    override fun print(data: PrintData) {
        out.println(data.format(cfg.monochrome))
    }
}
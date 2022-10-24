package ru.vm.mpb.printer

import org.fusesource.jansi.AnsiPrintStream
import ru.vm.mpb.config.MpbConfig

class UpdatingPrinter(val out: AnsiPrintStream, val cfg: MpbConfig): Printer {
    override suspend fun print(data: PrintData) {
        TODO("Not yet implemented")
    }

    override fun close() {
    }
}
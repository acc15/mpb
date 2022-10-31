package ru.vm.mpb.printer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.fusesource.jansi.AnsiPrintStream
import ru.vm.mpb.config.MpbConfig

interface Printer {
    fun print(data: PrintData)
}

fun CoroutineScope.createPrinter(cfg: MpbConfig, out: AnsiPrintStream): ChannelPrinter {
    val channel = Channel<PrintData>(1000)
    val printer = if (cfg.output.plain) DefaultPrinter(out) else StatusPrinter(out, cfg)

    launch {
        for (data in channel) {
            printer.print(data)
        }
    }

    return ChannelPrinter(channel)
}

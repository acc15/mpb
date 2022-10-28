package ru.vm.mpb.printer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.fusesource.jansi.AnsiConsole
import org.fusesource.jansi.AnsiPrintStream
import org.fusesource.jansi.AnsiType
import ru.vm.mpb.config.MpbConfig
import ru.vm.mpb.config.OutputConfig

interface Printer {
    fun print(data: PrintData)
}

fun CoroutineScope.createPrinter(cfg: MpbConfig, out: AnsiPrintStream): ChannelPrinter {
    val channel = Channel<PrintData>(1000)
    val printer = if (cfg.output.plain)
        DefaultPrinter(out, cfg.output) else StatusPrinter(out, cfg.output)

    launch {
        for (data in channel) {
            printer.print(data)
        }
    }

    return ChannelPrinter(channel)
}

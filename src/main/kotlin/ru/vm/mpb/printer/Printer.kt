package ru.vm.mpb.printer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.fusesource.jansi.AnsiConsole
import org.fusesource.jansi.AnsiType
import ru.vm.mpb.config.MpbConfig

interface Printer {
    fun print(data: PrintData)
}

private val UNSUPPORTED_TYPES = setOf(
    AnsiType.Unsupported,
    AnsiType.Redirected
)

fun CoroutineScope.createPrinter(cfg: MpbConfig): ChannelPrinter {
    val channel = Channel<PrintData>(1000)
    val out = AnsiConsole.out()

    val ansiSupported = !UNSUPPORTED_TYPES.contains(out.type)
    val colors = !cfg.output.monochrome && ansiSupported
    val formatter: PrintFormatter = { it.format(colors) }
    val printer = if (!cfg.output.plain && ansiSupported)
        StatusPrinter(out, formatter) else DefaultPrinter(out, formatter)

    launch {
        for (data in channel) {
            printer.print(data)
        }
    }
    return ChannelPrinter(channel)
}

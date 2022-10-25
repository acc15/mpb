package ru.vm.mpb.printer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.fusesource.jansi.AnsiConsole
import org.fusesource.jansi.AnsiType
import ru.vm.mpb.config.MpbConfig
import ru.vm.mpb.config.OutputConfig

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

    val outputConfig = cfg.output.withAnsiSupport(UNSUPPORTED_TYPES.contains(out.type))
    val printer = if (outputConfig.plain)
        DefaultPrinter(out, outputConfig) else StatusPrinter(out, outputConfig)

    launch {
        for (data in channel) {
            printer.print(data)
        }
    }
    return ChannelPrinter(channel)
}

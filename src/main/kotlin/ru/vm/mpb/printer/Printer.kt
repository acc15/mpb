package ru.vm.mpb.printer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.fusesource.jansi.AnsiConsole
import org.fusesource.jansi.AnsiType
import ru.vm.mpb.config.MpbConfig

interface Printer: AutoCloseable {
    suspend fun print(data: PrintData)
}

fun CoroutineScope.createPrinter(cfg: MpbConfig): Printer {
    val channel = Channel<PrintData>()

    val out = AnsiConsole.out()

    // TODO add opt-in for updating printer
    val p = if (out.type == AnsiType.Unsupported || out.type == AnsiType.Redirected)
        DefaultPrinter(out, cfg) else UpdatingPrinter(out, cfg)

    launch {
        for (data in channel) {
            p.print(data)
        }
    }
    return ChannelPrinter(channel)
}

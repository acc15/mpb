package ru.vm.mpb.cmd.ctx

import kotlinx.coroutines.sync.Semaphore
import org.fusesource.jansi.Ansi
import ru.vm.mpb.config.MpbConfig
import ru.vm.mpb.printer.Printer
import ru.vm.mpb.printer.PrintData
import ru.vm.mpb.printer.PrintStatus
import ru.vm.mpb.util.redirectBoth

data class CmdContext(val cfg: MpbConfig, val printer: Printer) {

    val args = cfg.args.common
    val ansi: Ansi get() = cfg.output.ansi.get()
    val sessionSemaphore = if (cfg.maxSessions > 0) Semaphore(cfg.maxSessions) else null
    fun ansi(parent: Ansi) = cfg.output.ansi.get(parent)

    fun print(str: Any?, status: PrintStatus = PrintStatus.MESSAGE, key: String = "*") {
        printer.print(PrintData(key, ansi.apply(status.colored("[$key]")).a(' ').a(str).toString()))
    }

    fun projectContext(key: String) = ProjectContext(this, key)

    fun exec(vararg cmdline: String) = ProcessBuilder(*cmdline).also(this::applyContext)
    fun exec(cmdline: List<String>) = ProcessBuilder(cmdline).also(this::applyContext)

    private fun applyContext(b: ProcessBuilder) {
        b.redirectBoth(if (cfg.debug) ProcessBuilder.Redirect.INHERIT else ProcessBuilder.Redirect.DISCARD)
    }

}
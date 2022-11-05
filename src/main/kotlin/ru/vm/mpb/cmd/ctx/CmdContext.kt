package ru.vm.mpb.cmd.ctx

import org.fusesource.jansi.Ansi
import ru.vm.mpb.config.MpbConfig
import ru.vm.mpb.printer.Printer
import ru.vm.mpb.printer.PrintData
import ru.vm.mpb.printer.PrintStatus
import ru.vm.mpb.util.redirectBoth

data class CmdContext(val cfg: MpbConfig, val printer: Printer) {

    val args = cfg.args.common

    val ansi: Ansi get() = cfg.output.ansi.get()
    fun ansi(parent: Ansi) = cfg.output.ansi.get(parent)

    fun print(str: Any?, status: PrintStatus = PrintStatus.MESSAGE, key: String = "*") {
        printer.print(PrintData(key, ansi.apply(status.colored("[$key]")).a(' ').a(str).toString()))
    }

    fun projectContext(key: String) = ProjectContext(this, key)

    fun exec(vararg cmdline: String) = applyDebug(ProcessBuilder(*cmdline))
    fun exec(cmdline: List<String>) = applyDebug(ProcessBuilder(cmdline))

    private fun applyDebug(b: ProcessBuilder): ProcessBuilder {
        if (cfg.debug) {
            b.redirectBoth(ProcessBuilder.Redirect.INHERIT)
        }
        return b
    }

}
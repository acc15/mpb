package ru.vm.mpb.cmd.ctx

import org.fusesource.jansi.Ansi
import ru.vm.mpb.util.ProcessExecutor
import ru.vm.mpb.config.MpbConfig
import ru.vm.mpb.jansi.NoColorAnsi
import ru.vm.mpb.printer.Printer
import ru.vm.mpb.printer.PrintData
import ru.vm.mpb.printer.PrintStatus

data class CmdContext(val cfg: MpbConfig, val printer: Printer) {

    val args = cfg.args.common

    val ansi: Ansi get() = if (cfg.output.monochrome && !cfg.output.noAnsi) NoColorAnsi() else Ansi.ansi()

    fun print(str: Any?, status: PrintStatus = PrintStatus.MESSAGE, key: String = "") {
        val msg = ansi
        if (key.isNotEmpty()) {
            msg.bold().a('[').also { status.ansiColor(it) }.a(key).fgDefault().a(']').reset()
        }
        msg.a(' ').a(str)
        printer.print(PrintData(msg.toString(), key))
    }

    fun projectContext(key: String) = ProjectContext(this, key)

    fun exec(vararg cmdline: String) = exec(ProcessBuilder(*cmdline))
    fun exec(cmdline: List<String>) = exec(ProcessBuilder(cmdline))

    private fun exec(b: ProcessBuilder): ProcessExecutor {
        val e = ProcessExecutor(b)
        if (cfg.debug) {
            e.redirectTo(ProcessBuilder.Redirect.INHERIT)
        }
        return e
    }

}
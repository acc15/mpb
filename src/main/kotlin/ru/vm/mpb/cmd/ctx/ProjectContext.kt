package ru.vm.mpb.cmd.ctx

import org.fusesource.jansi.Ansi
import ru.vm.mpb.printer.PrintStatus

data class ProjectContext(
    val cmd: CmdContext,
    val key: String,
) {

    val ansi get() = cmd.ansi
    fun ansi(parent: Ansi) = cmd.ansi(parent)

    val cfg = cmd.cfg
    val info = cfg.projects.getValue(key)
    val args = cfg.args.active[key].orEmpty()
    val build = info.build
    val skipped = !cfg.args.active.containsKey(key)

    fun print(str: Any?, status: PrintStatus = PrintStatus.MESSAGE, key: String = this.key) =
        cmd.print(str, status, key)

    fun exec(vararg cmdline: String) = cmd.exec(*cmdline).directory(info.dir.toFile())
    fun exec(cmdline: List<String>) = cmd.exec(cmdline).directory(info.dir.toFile())

}
package ru.vm.mpb.cmd.ctx

import ru.vm.mpb.printer.PrintStatus

data class ProjectContext(
    val cmd: CmdContext,
    val key: String,
) {

    val cfg = cmd.cfg
    val info = cfg.projects.getValue(key)
    val args = cfg.args.active[key].orEmpty()
    val build = info.build

    fun print(str: Any?, status: PrintStatus = PrintStatus.MESSAGE, key: String = this.key) =
        cmd.print(str, status, key)

    fun exec(vararg cmdline: String) = cmd.exec(*cmdline).dir(info.dir)
    fun exec(cmdline: List<String>) = cmd.exec(cmdline).dir(info.dir)

}
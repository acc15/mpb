package ru.vm.mpb.cmd.ctx

import ru.vm.mpb.config.ProjectConfig
import ru.vm.mpb.printer.PrintData

data class ProjectContext(
    val cmd: CmdContext,
    val key: String,
) {

    val cfg = cmd.cfg
    val info: ProjectConfig = cfg.projects.getValue(key)
    val args = cfg.activeArgs[key].orEmpty()
    val build = cfg.build.getValue(info.build)

    fun print(str: Any?, key: String = this.key) = cmd.print(str, key)

    fun exec(vararg cmdline: String) = cmd.exec(*cmdline).dir(info.dir)
    fun exec(cmdline: List<String>) = cmd.exec(cmdline).dir(info.dir)

}
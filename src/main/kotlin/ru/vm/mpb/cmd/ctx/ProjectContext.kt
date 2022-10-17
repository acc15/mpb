package ru.vm.mpb.cmd.ctx

import ru.vm.mpb.config.ProjectConfig

data class ProjectContext(
    val cmd: CmdContext,
    val key: String,
) {

    val cfg = cmd.cfg
    val info: ProjectConfig = cfg.projects.getValue(key)
    val args = cfg.activeArgs[key].orEmpty()

    fun print(str: Any?, e: Throwable? = null) = cmd.print(str, e, key)

    fun exec(vararg cmdline: String) = cmd.exec(*cmdline).dir(info.dir)
    fun exec(cmdline: List<String>) = cmd.exec(cmdline).dir(info.dir)

}
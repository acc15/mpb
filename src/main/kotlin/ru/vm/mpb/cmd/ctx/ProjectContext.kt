package ru.vm.mpb.cmd.ctx

import ru.vm.mpb.config.ProjectConfig
import ru.vm.mpb.util.ProcessExec

data class ProjectContext(
    val cmd: CmdContext,
    val key: String,
) {

    val cfg = cmd.cfg
    val info: ProjectConfig = cfg.projects.getValue(key)
    val exec = ProcessExec(info.dir, cfg.debug)
    val args = cfg.activeArgs[key].orEmpty()

    fun print(str: Any?, e: Throwable? = null) = cmd.print(str, e, key)

    fun exec(vararg cmdline: String, customizer: (ProcessBuilder) -> Unit = cmd::defaultCustomizer) =
        cmd.exec(info.dir, *cmdline, customizer = customizer)

    fun exec(cmdline: List<String>, customizer: (ProcessBuilder) -> Unit = cmd::defaultCustomizer) =
        cmd.exec(info.dir, cmdline, customizer)

}
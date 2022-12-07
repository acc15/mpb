package ru.vm.mpb.cmd.ctx

import kotlinx.coroutines.sync.withPermit
import org.fusesource.jansi.Ansi
import ru.vm.mpb.printer.PrintStatus
import ru.vm.mpb.util.redirectBoth

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

    fun exec(vararg cmdline: String) = cmd.exec(*cmdline).also(this::applyContext)
    fun exec(cmdline: List<String>) = cmd.exec(cmdline).also(this::applyContext)

    private fun applyContext(b: ProcessBuilder) {
        b.directory(info.dir.toFile()).redirectBoth(defaultRedirect)
    }

    private val defaultRedirect = if (cfg.debug)
        ProcessBuilder.Redirect.to(info.log.toFile()) else
        ProcessBuilder.Redirect.DISCARD

    suspend fun <T> withMaxSessions(block: ProjectContext.() -> T): T = if (cmd.sessionSemaphore != null)
        cmd.sessionSemaphore.withPermit { this.block() } else block()

}
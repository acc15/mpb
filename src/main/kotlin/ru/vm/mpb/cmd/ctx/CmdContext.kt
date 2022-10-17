package ru.vm.mpb.cmd.ctx

import ru.vm.mpb.ProcessExecutor
import ru.vm.mpb.config.MpbConfig
import java.io.PrintStream

data class CmdContext(val cfg: MpbConfig, val out: PrintStream = System.out) {

    val args = cfg.commonArgs

    fun print(str: Any?, e: Throwable? = null, key: String? = null) {
        out.println(key?.let { "[${it}] $str" } ?: str)
        if (cfg.debug && e != null) {
            e.printStackTrace(out)
        }
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
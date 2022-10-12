package ru.vm.mpb.cmd.ctx

import ru.vm.mpb.config.MpbConfig
import java.io.File
import java.io.PrintStream

data class CmdContext(val cfg: MpbConfig, val out: PrintStream = System.out) {

    val args = cfg.commonArgs

    fun print(str: Any?, e: Throwable? = null, key: String? = null) {
        out.println(key?.map { "[${it}] $str" } ?: str)
        if (cfg.debug && e != null) {
            e.printStackTrace(out)
        }
    }

    fun projectContext(key: String) = ProjectContext(this, key)

    fun exec(dir: File, vararg cmdline: String, customizer: (ProcessBuilder) -> Unit = this::defaultCustomizer) =
        exec(ProcessBuilder(*cmdline).directory(dir), customizer)

    fun exec(dir: File, cmdline: List<String>, customizer: (ProcessBuilder) -> Unit = this::defaultCustomizer) =
        exec(ProcessBuilder(cmdline).directory(dir), customizer)

    private fun exec(b: ProcessBuilder, customizer: (ProcessBuilder) -> Unit): Boolean {
        customizer(b)
        return b.start().waitFor() == 0
    }

    fun defaultCustomizer(b: ProcessBuilder) {
        if (cfg.debug) {
            b.redirectError(ProcessBuilder.Redirect.INHERIT)
        }
    }

}
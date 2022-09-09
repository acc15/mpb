package ru.vm.mpb.util

import java.io.File

fun runProcess(cmd: List<String>, dir: File,  customizer: (ProcessBuilder) -> Unit = {}): Boolean {
    val p = ProcessBuilder(cmd).directory(dir)
    customizer(p)
    return p.start().waitFor() == 0
}

fun redirectErrorsIf(cond: Boolean): ((ProcessBuilder) -> Unit) = if (cond)
        ({ it.redirectError(ProcessBuilder.Redirect.INHERIT) })
else
        ({})

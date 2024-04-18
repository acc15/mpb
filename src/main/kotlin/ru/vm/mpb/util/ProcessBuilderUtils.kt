package ru.vm.mpb.util

import java.io.File
import java.lang.ProcessBuilder.Redirect

fun ProcessBuilder.redirectBoth(file: File): ProcessBuilder = redirectError(file).redirectOutput(file)
fun ProcessBuilder.redirectBoth(redirect: Redirect): ProcessBuilder = redirectError(redirect).redirectOutput(redirect)
fun ProcessBuilder.run() { success() }
fun ProcessBuilder.success() = start().waitFor() == 0
fun ProcessBuilder.environment(map: Map<String, String>): ProcessBuilder {
    environment().putAll(map)
    return this
}

fun ProcessBuilder.lines(): List<String> {
    val p = redirectBoth(Redirect.PIPE).start()
    val lines = p.inputReader().readLines()
    val exitCode = p.waitFor()
    if (exitCode != 0) {
        throw RuntimeException(
            "Error while executing command (exit code: ${exitCode}): ${command().joinToString(" ")}")
    }
    return lines
}

package ru.vm.mpb.util

import java.io.File
import java.lang.ProcessBuilder.Redirect

fun ProcessBuilder.redirectBoth(file: File): ProcessBuilder = redirectError(file).redirectOutput(file)
fun ProcessBuilder.redirectBoth(redirect: Redirect): ProcessBuilder = redirectError(redirect).redirectOutput(redirect)
fun ProcessBuilder.success() = start().waitFor() == 0
fun ProcessBuilder.environment(map: Map<String, String>): ProcessBuilder {
    environment().putAll(map)
    return this
}

fun ProcessBuilder.lines(): Sequence<String> {
    val p = redirectBoth(Redirect.PIPE).start()
    val s = p.waitFor()
    return if (s == 0) p.inputReader().lineSequence() else emptySequence()
}

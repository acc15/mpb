package ru.vm.mpb

import java.io.File
import java.io.InputStreamReader
import java.lang.ProcessBuilder.Redirect

class ProcessExecutor(val builder: ProcessBuilder) {

    fun dir(str: String) = dir(File(str))
    fun dir(dir: File): ProcessExecutor {
        builder.directory(dir)
        return this
    }

    fun customize(customizer: (ProcessBuilder) -> Unit): ProcessExecutor {
        customizer(builder)
        return this
    }

    fun redirectTo(file: File) = redirectTo(Redirect.to(file))

    fun redirectTo(redirect: Redirect): ProcessExecutor {
        builder.redirectError(redirect).redirectOutput(redirect)
        return this
    }

    fun env(customizer: (MutableMap<String, String>) -> Unit): ProcessExecutor {
        customizer(builder.environment())
        return this
    }

    fun env(map: Map<String, String>): ProcessExecutor {
        builder.environment().putAll(map)
        return this
    }

    fun start() = builder.start().waitFor()
    fun success() = start() == 0
    fun lines(): List<String> {
        val p = builder.start()
        val exitCode = p.waitFor()
        return if (exitCode == 0) InputStreamReader(p.inputStream).readLines() else emptyList()
    }

}
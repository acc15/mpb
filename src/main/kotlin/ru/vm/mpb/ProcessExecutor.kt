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

    fun start() = builder.start()
    fun wait() = start().waitFor()
    fun success() = wait() == 0
    fun lines(): List<String> = builder.start().let {
        val exitCode = it.waitFor()
        return if (exitCode == 0) InputStreamReader(it.inputStream).readLines() else emptyList()
    }

    fun readLines(handler: (String) -> Unit): Int {
        return builder.start()
            .also { InputStreamReader(it.inputStream).forEachLine(handler) }
            .waitFor()
    }

}
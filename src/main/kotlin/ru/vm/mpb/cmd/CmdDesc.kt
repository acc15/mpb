package ru.vm.mpb.cmd

import org.fusesource.jansi.Ansi.Consumer
import ru.vm.mpb.ansi.applyIf
import ru.vm.mpb.ansi.join

data class CmdDesc(
    val names: List<String>,
    val description: String,
    val argDescription: String
) {
    fun help(programName: String) = Consumer { a ->
        a.join(names) { _, it -> a.bold().a(it).boldOff() }.a(" - ").a(description).a(". ").apply(usage(programName))
    }

    fun usage(programName: String) = Consumer { a ->
        a.bold().a("Usage: ").boldOff().a(programName).a(' ').a(names[0])
            .applyIf(argDescription.isNotEmpty()) { it.a(' ').a(argDescription) }
    }
}

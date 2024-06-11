package ru.vm.mpb.commands

import com.github.ajalt.clikt.completion.completionOption
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.path
import java.nio.file.Path
import com.github.ajalt.clikt.parameters.groups.provideDelegate

class MpbCommand : CliktCommand() {

    /** Config file location */
    val config: Path by option("-c", "--config", help = "Config file location").path().default(Path.of("mpb.yaml"))

    /** Output options */
    val output by OutputOptions()

    /** Project options */
    val project by ProjectOptions()

    override fun aliases(): Map<String, List<String>> = mapOf(
        "b" to listOf("build"),
        "p" to listOf("pull"),
        "c" to listOf("switch"),
        "s" to listOf("switch")
    )

    override fun run() {
        println(output)
        println(project)
    }
}

fun main(args: Array<String>) = MpbCommand()
    .completionOption()
    .subcommands(
        BuildCommand(),
        SwitchCommand(),
        PullCommand()
    )
    .main(args)

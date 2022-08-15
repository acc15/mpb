package ru.vm.mpb.cmd

import ru.vm.mpb.PROGRAM_NAME
import ru.vm.mpb.config.MpbConfig
import kotlin.system.exitProcess

abstract class Cmd(
    val names: Set<String>,
    val description: String,
    val argDescription: String,
) {

    val sortedNames: List<String> = names.sortedBy { it.length }

    val help: String
        get() = "${sortedNames.joinToString(", ")} - ${description}. $usage"

    val usage: String
        get() = "Usage: $PROGRAM_NAME ${sortedNames[0]}" + if (argDescription.isEmpty()) "" else " $argDescription"

    abstract fun execute(cfg: MpbConfig, args: List<String>)


    fun printUsageAndExit(): Nothing {
        println(usage)
        exitProcess(1)
    }

}




package ru.vm.mpb.cmd

import ru.vm.mpb.PROGRAM_NAME

data class CmdDesc(
    val names: Set<String>,
    val description: String,
    val argDescription: String
) {

    val sortedNames: List<String> = names.sortedBy { it.length }

    val help: String
        get() = "${sortedNames.joinToString(", ")} - ${description}. $usage"

    val usage: String
        get() = "Usage: $PROGRAM_NAME ${sortedNames[0]}" + if (argDescription.isEmpty()) "" else " $argDescription"

}

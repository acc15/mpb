package ru.vm.mpb.cmd

import ru.vm.mpb.config.MpbConfig

data class CmdDesc(
    val names: Set<String>,
    val description: String,
    val argDescription: String
) {

    val sortedNames: List<String> = names.sortedBy { it.length }

    fun help(cfg: MpbConfig) = "${sortedNames.joinToString(", ")} - ${description}. ${usage(cfg)}"
    fun usage(cfg: MpbConfig) = "Usage: ${cfg.name} ${sortedNames[0]}" +
            if (argDescription.isEmpty()) "" else " $argDescription"

}

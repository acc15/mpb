package ru.vm.mpb.cmd

import ru.vm.mpb.config.MpbConfig

data class CmdDesc(
    val names: List<String>,
    val description: String,
    val argDescription: String
) {
    fun help(cfg: MpbConfig) = "${names.joinToString(", ")} - ${description}. ${usage(cfg)}"
    fun usage(cfg: MpbConfig) = "Usage: ${cfg.name} ${names[0]}" +
            if (argDescription.isEmpty()) "" else " $argDescription"
}

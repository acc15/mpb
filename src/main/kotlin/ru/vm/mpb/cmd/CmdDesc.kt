package ru.vm.mpb.cmd

import org.fusesource.jansi.Ansi
import ru.vm.mpb.config.MpbConfig

data class CmdDesc(
    val names: List<String>,
    val description: String,
    val argDescription: String
) {
    fun help(cfg: MpbConfig) = names.joinToString(", ") {
        Ansi.ansi().bold().a(it).boldOff().toString()
    } + " - ${description}. ${usage(cfg)}"

    fun usage(cfg: MpbConfig): String {
        val a = Ansi.ansi().bold().a("Usage: ").boldOff().a(cfg.name).a(' ').a(names[0])
        if (argDescription.isNotEmpty()) {
            a.a(' ').a(argDescription)
        }
        return a.toString()
    }
}

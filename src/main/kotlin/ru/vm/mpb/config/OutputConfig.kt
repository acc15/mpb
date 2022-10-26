package ru.vm.mpb.config

import org.fusesource.jansi.AnsiConsole
import org.fusesource.jansi.AnsiType
import ru.vm.mpb.config.state.Config

private val UNSUPPORTED_TYPES = setOf(
    AnsiType.Unsupported,
    AnsiType.Redirected
)

data class OutputConfig(
    val plain: Boolean,
    val monochrome: Boolean,
    val width: Int
) {
    fun getWidth(terminalWidth: Int) = if (terminalWidth <= 0) width else terminalWidth
    companion object {
        fun fromConfig(cfg: Config): OutputConfig {
            val ansiUnsupported = UNSUPPORTED_TYPES.contains(AnsiConsole.out().type)
            return OutputConfig(
                cfg.get("status").flag || ansiUnsupported,
                cfg.get("colors").flag || ansiUnsupported,
                cfg.get("width").int ?: 80
            )
        }
    }
}
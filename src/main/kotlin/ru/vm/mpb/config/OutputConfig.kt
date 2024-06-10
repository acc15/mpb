package ru.vm.mpb.config

import org.fusesource.jansi.AnsiConsole
import org.fusesource.jansi.AnsiType
import ru.vm.mpb.config.state.Config
import ru.vm.mpb.ansi.ansi
import ru.vm.mpb.config.state.ConfigShorthand

private val UNSUPPORTED_TYPES = setOf(
    AnsiType.Unsupported,
    AnsiType.Redirected
)

data class OutputConfig(
    val plain: Boolean,
    val monochrome: Boolean,
    val width: Int,
) {

    val ansi = ansi(!monochrome)

    fun getWidth(terminalWidth: Int) = if (terminalWidth <= 0) width else terminalWidth
    companion object {
        fun fromConfig(cfg: Config): OutputConfig {
            val noAnsi = UNSUPPORTED_TYPES.contains(AnsiConsole.out().type) || cfg.shorthand.get("noAnsi").flag
            return OutputConfig(
                cfg.shorthand.get("plain").flag || noAnsi,
                cfg.shorthand.get("monochrome").flag || noAnsi,
                cfg.shorthand.get("width").int ?: 80
            )
        }
    }
}
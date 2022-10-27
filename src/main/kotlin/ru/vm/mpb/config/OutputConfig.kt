package ru.vm.mpb.config

import org.fusesource.jansi.AnsiConsole
import org.fusesource.jansi.AnsiType
import ru.vm.mpb.config.state.Config

private val UNSUPPORTED_TYPES = setOf(
    AnsiType.Unsupported,
    AnsiType.Redirected
)

data class OutputConfig(
    val noAnsi: Boolean,
    val plain: Boolean,
    val monochrome: Boolean,
    val width: Int,
) {
    fun getWidth(terminalWidth: Int) = if (terminalWidth <= 0) width else terminalWidth
    companion object {
        fun fromConfig(cfg: Config): OutputConfig {
            val noAnsi = UNSUPPORTED_TYPES.contains(AnsiConsole.out().type) || cfg.get("noAnsi").flag
            return OutputConfig(
                noAnsi,
                cfg.get("plain").flag || noAnsi,
                cfg.get("monochrome").flag || noAnsi,
                cfg.get("width").int ?: 80
            )
        }
    }
}
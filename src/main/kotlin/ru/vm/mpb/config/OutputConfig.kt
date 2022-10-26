package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config

data class OutputConfig(
    val plain: Boolean,
    val monochrome: Boolean,
    val width: Int
) {
    fun withAnsiSupport(ansiUnsupported: Boolean) = OutputConfig(
        plain || ansiUnsupported,
        monochrome || ansiUnsupported,
        width
    )

    fun getWidth(providedWidth: Int): Int {
        return if (providedWidth == 0) width else providedWidth
    }

    companion object {
        fun fromConfig(cfg: Config) = OutputConfig(
            cfg.get("status").flag,
            cfg.get("colors").flag,
            cfg.get("width").int ?: 80
        )
    }
}
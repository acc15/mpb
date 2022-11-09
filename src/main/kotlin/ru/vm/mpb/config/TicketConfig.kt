package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config
import java.io.File
import java.nio.file.Path

data class TicketConfig(
    val dir: Path,
    val overwrite: Boolean
) {
    companion object {
        fun fromConfig(cfg: Config, base: Path) = TicketConfig(
            base.resolve(cfg.get("dir").string ?: "tickets"),
            cfg.get("overwrite").flag
        )
    }
}
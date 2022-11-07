package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config
import java.io.File

data class TicketConfig(
    val dir: File,
    val overwrite: Boolean
) {
    companion object {
        fun fromConfig(cfg: Config, base: File) = TicketConfig(
            base.resolve(cfg.get("dir").file ?: File("tickets")),
            cfg.get("overwrite").flag
        )
    }
}
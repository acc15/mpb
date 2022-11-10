package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config
import java.nio.file.Path
import kotlin.io.path.Path

data class PathConfig(
    val base: Path,
    val log: Path
) {
    companion object {
        fun fromConfig(cfg: Config): PathConfig {
            val base = cfg.shorthand.get("base").path ?: MpbEnv.home
            return PathConfig(
                base,
                base.resolve(cfg.shorthand.get("log").path ?: Path("log"))
            )
        }
    }
}
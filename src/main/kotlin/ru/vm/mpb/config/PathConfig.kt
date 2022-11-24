package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config
import java.nio.file.Path
import kotlin.io.path.Path

data class PathConfig(
    /**
     * Common base path
     */
    val base: Path,

    /**
     * Log dir (relative to base)
     */
    val log: Path,

    /**
     * Projects dir (relative to base)
     */
    val project: Path

) {
    companion object {
        fun fromConfig(cfg: Config): PathConfig {
            val base = cfg.shorthand.get("base").path ?: MpbEnv.home
            return PathConfig(
                base,
                base.resolve(cfg.shorthand.get("log").path ?: Path("log")),
                cfg.get("project").path?.let(base::resolve) ?: base
            )
        }
    }
}
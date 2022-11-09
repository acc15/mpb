package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path

data class PathConfig(
    val base: Path,
    val log: Path,
    val cd: Path
) {
    companion object {
        fun fromConfig(cfg: Config, base: Path) = PathConfig(
            base,
            base.resolve(cfg.get("log").string ?: "log"),
            base.resolve(cfg.get("cd").path ?: Path(System.getProperty("java.io.tmpdir"), "mpb_cd.txt"))
        )
    }
}
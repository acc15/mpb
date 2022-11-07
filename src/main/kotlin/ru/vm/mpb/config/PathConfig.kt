package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config
import java.io.File

data class PathConfig(
    val base: File,
    val log: File,
    val cd: File
) {
    companion object {
        fun fromConfig(cfg: Config, base: File) = PathConfig(
            base,
            base.resolve(cfg.get("log").file ?: File("log")),
            base.resolve(cfg.get("cd").file ?: File(System.getProperty("java.io.tmpdir")).resolve("mpb_cd.txt"))
        )
    }
}
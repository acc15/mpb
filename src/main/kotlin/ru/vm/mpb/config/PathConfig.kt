package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config
import java.io.File

data class PathConfig(
    val config: File,
    val base: File,
    val log: File,
    val cd: File
) {
    companion object {
        fun fromConfig(cfg: Config, file: File) = PathConfig(
            file,
            cfg.get("base").file ?: file.parentFile,
            cfg.get("log").file ?: File("log"),
            cfg.get("cd").file ?: File(System.getProperty("java.io.tmpdir")).resolve("mpb_cd.txt")
        )
    }
}
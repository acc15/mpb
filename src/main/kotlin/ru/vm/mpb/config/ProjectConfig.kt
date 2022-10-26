package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config
import java.io.File

data class ProjectConfig(
    val dir: File,
    val deps: Set<String>,
    val build: BuildConfig,
    val branch: BranchConfig,
    val log: File
) {
    companion object {
        fun fromConfig(key: String, cfg: Config, path: PathConfig, build: Config) = ProjectConfig(
            path.base.resolve(cfg.get("dir").file ?: File(key)),
            cfg.get("deps").stringSet,
            BuildConfig.fromConfig(cfg.get("build"), build, DEFAULT_KEY),
            BranchConfig.fromConfig(cfg.get("branch")),
            path.log.resolve(cfg.get("log").file ?: File("$key.log"))
        )
    }
}
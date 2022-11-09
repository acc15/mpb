package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config
import java.io.File
import java.nio.file.Path

data class ProjectConfig(
    val dir: Path,
    val deps: Set<String>,
    val build: BuildConfig,
    val branch: BranchConfig,
    val log: Path
) {
    companion object {
        fun fromConfig(key: String, cfg: Config, path: PathConfig, root: Config) = ProjectConfig(
            path.base.resolve(cfg.get("dir").string ?: key),
            cfg.get("deps").stringSet,
            BuildConfig.fromConfig(BuildConfig.merge(cfg, root, DEFAULT_KEY)),
            BranchConfig.fromConfig(Config.mergeAll(root.get("branch"), cfg.get("branch"))),
            path.log.resolve(cfg.get("log").string ?: "$key.log")
        )
    }
}
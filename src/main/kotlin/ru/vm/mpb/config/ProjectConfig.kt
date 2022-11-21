package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config
import java.nio.file.Path

data class ProjectConfig(
    val dir: Path,
    val deps: Set<String>,
    val noSkip: Boolean, // don't propagate skip statuses
    val build: BuildConfig,
    val branch: GitConfig,
    val log: Path
) {
    companion object {
        fun fromConfig(key: String, project: Config, root: Config, path: PathConfig) = ProjectConfig(
            path.base.resolve(project.get("dir").string ?: key),
            project.get("deps").stringSet,
            project.shorthand.get("ignoreDeps").flag,
            BuildConfig.fromConfig(BuildConfig.merge(root.get("build"), project.get("build"), DEFAULT_KEY)),
            GitConfig.fromConfig(Config.mergeAll(root.get("git"), project.get("git")).shorthand(root)),
            path.log.resolve(project.get("log").string ?: "$key.log")
        )
    }
}
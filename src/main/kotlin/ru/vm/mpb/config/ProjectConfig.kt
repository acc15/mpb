package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config
import java.nio.file.Path

data class ProjectConfig(
    val dir: Path,
    val deps: Set<String>,
    val build: BuildConfig,
    val branch: BranchConfig,
    val log: Path
) {
    companion object {
        fun fromConfig(key: String, project: Config, root: Config, path: PathConfig) = ProjectConfig(
            path.base.resolve(project.get("dir").string ?: key),
            project.get("deps").stringSet,
            BuildConfig.fromConfig(BuildConfig.merge(project.get("build"), root.get("build"), DEFAULT_KEY)),
            BranchConfig.fromConfig(Config.mergeAll(root.get("branch"), project.get("branch")).shorthand(root)),
            path.log.resolve(project.get("log").string ?: "$key.log")
        )
    }
}
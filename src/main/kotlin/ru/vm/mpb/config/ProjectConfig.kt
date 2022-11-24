package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config
import java.nio.file.Path
import kotlin.io.path.Path

data class ProjectConfig(

    /**
     * project baseDir
     */
    val dir: Path,

    /**
     * Project dependencies
     */
    val deps: Set<String>,

    /**
     * Propagate skip statuses (disabled by default).
     * If enabled and one of dependencies was skipped - this project will be also skipped
     */
    val propagateSkip: Boolean,

    /**
     * Project build configuration
     */
    val build: BuildConfig,

    /**
     * Project git configuration
     */
    val git: GitConfig,

    /**
     * Project log file
     */
    val log: Path

) {
    companion object {
        fun fromConfig(key: String, project: Config, root: Config, path: PathConfig) = ProjectConfig(
            path.project.resolve(project.get("dir").path ?: Path(key)),
            project.get("deps").stringSet,
            project.shorthand.get("propagateSkip").flag,
            BuildConfig.fromConfig(BuildConfig.merge(root.get("build"), project.get("build"), DEFAULT_KEY)),
            GitConfig.fromConfig(Config.mergeAll(root.get("git"), project.get("git")).shorthand(root)),
            path.log.resolve(project.get("log").string ?: "$key.log")
        )
    }
}
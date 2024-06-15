package ru.vm.mpb.configuration;

import kotlinx.serialization.Serializable;
import java.nio.file.Path

@Serializable
data class ProjectConfig(
    /** Project directory. Relative to [PathConfig.projects]. Defaults to `${project.key}` */
    val dir: Path?,

    /** Project log file path. Relative to [PathConfig.logs]. Defaults to `${project.key}.log` */
    val log: Path?,

    /** Dependencies */
    val deps: Set<String> = emptySet(),

    /** Build tool name */
    val tool: String = "default",

    /** Git configuration */
    val git: ProjectGitConfig = ProjectGitConfig()
)

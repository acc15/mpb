package ru.vm.mpb.configuration

import kotlinx.serialization.Serializable

@Serializable
data class MpbConfig(
    /** Configuration name (used for self-referencing) */
    val name: String,

    /** Tools will print all message to stdout */
    val debug: Boolean = false,

    /** Run tools sequentially (by default they run in parallel) */
    val seq: Boolean = false,

    /** Paths configuration */
    val path: PathConfig = PathConfig(),

    /** Global git configuration */
    val git: GitConfig = GitConfig(),

    /** Projects */
    val projects: Map<String, ProjectConfig> = emptyMap(),

    /** Build tools, options, environment and commands */
    val tools: Map<String, ToolConfig> = emptyMap()
)
package ru.vm.mpb.configuration

import kotlinx.serialization.Serializable
import ru.vm.mpb.regex.BranchPattern
import java.nio.file.Path

@Serializable
data class ProjectGitConfig(
    /**
     * Default branch name (for switching, etc)
     *
     * Overrides top level [GitConfig.default]
     */
    val default: String? = null,

    /**
     * List of branch patterns for quick switching to related branches
     *
     * Overrides top level [GitConfig.patterns]
     */
    val patterns: List<BranchPattern>? = null,

    /**
     * Disables fetching
     *
     * Overrides top level [GitConfig.noFetch]
     */
    val noFetch: Boolean? = null,

    /**
     * Disables rebasing
     *
     * Overrides top level [GitConfig.noRebase]
     */
    val noRebase: Boolean? = null,

    /**
     * Ignored paths
     *
     * Overrides top level [GitConfig.ignore]
     */
    val ignore: Set<Path>? = null
)

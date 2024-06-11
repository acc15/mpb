package ru.vm.mpb.configuration

import kotlinx.serialization.Serializable
import ru.vm.mpb.regex.BranchPattern
import java.nio.file.Path

@Serializable
data class GitConfig(
    /** Default branch name (for switching, etc) */
    val default: String = "master",

    /** List of branch patterns for quick switching to related branches */
    val patterns: List<BranchPattern> = emptyList(),

    /** Disables fetching */
    val noFetch: Boolean = false,

    /** Disables rebasing */
    val noRebase: Boolean = false,

    /** Ignored paths */
    val ignore: Set<Path> = emptySet(),

    /** Maximum session count (useful to limit maximum connection count to single server) */
    val maxSessions: Int = 0
)

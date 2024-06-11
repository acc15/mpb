package ru.vm.mpb.configuration

import kotlinx.serialization.Serializable
import ru.vm.mpb.regex.RegexSequence

@Serializable
data class BuildProgressConfig(
    /** Command to run to get build plan (how many steps, and their names */
    val cmd: List<String>,

    /** Regex sequence to parse build plan */
    val plan: RegexSequence,

    /** Regex sequence to build execution */
    val build: RegexSequence
)

package ru.vm.mpb.configuration

import kotlinx.serialization.Serializable
import java.nio.file.Path

@Serializable
data class PathConfig(
    /** Base directory. Relative to [config file location][ru.vm.mpb.commands.MpbCommand.config] */
    val base: Path = Path.of("log"),

    /** Directory for build logs. Relative to [base] dir */
    val logs: Path = Path.of("log"),

    /** Base dir for all projects. Relative to [base] dir */
    val projects: Path = Path.of("")
)

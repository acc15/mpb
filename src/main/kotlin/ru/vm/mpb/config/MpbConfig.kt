package ru.vm.mpb.config

import ru.vm.mpb.util.MessagePrinter
import java.nio.file.Path

typealias BuildCmdConfig = Map<String, List<String>>

data class MpbConfig(
    val defaultBranch: String,
    val debug: Boolean,
    val projects: Map<String, ProjectConfig>,
    val jira: JiraConfig,
    val ticket: TicketConfig,
    val build: BuildCmdConfig
) {
    val print = MessagePrinter(this)
    fun getDefaultBranch(proj: String): String = projects[proj]?.defaultBranch ?: defaultBranch
}

data class JiraConfig(
    val url: String,
    val project: String
)

data class TicketConfig(
    val dir: Path
)

data class ProjectConfig(
    val dir: Path,
    val deps: Set<String> = emptySet(),
    val build: BuildCmdConfig?,
    val defaultBranch: String?,
)
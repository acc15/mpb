package ru.vm.mpb.config

import ru.vm.mpb.util.MessagePrinter
import java.nio.file.Path

data class MpbConfig(
    val defaultBranch: String,
    val debug: Boolean,
    val projects: Map<String, ProjectConfig>,
    val jira: JiraConfig,
    val ticket: TicketConfig,
    val build: Map<String, BuildConfig>
) {
    val print = MessagePrinter(this)
    fun getDefaultBranch(proj: String): String = projects[proj]?.defaultBranch ?: defaultBranch
}

data class BuildConfig(
    val profiles: Map<String, List<String>>,
    val env: Map<String, String>?
)

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
    val build: String?,
    val defaultBranch: String?,
)
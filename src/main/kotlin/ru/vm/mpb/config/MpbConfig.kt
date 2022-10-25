package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config
import java.io.File

const val DEFAULT_KEY = "default"

data class MpbConfig(
    val name: String,
    val config: File,
    val cd: File,
    val output: OutputConfig,
    val debug: Boolean,
    val branch: BranchConfig,
    val projects: Map<String, ProjectConfig>,
    val jira: JiraConfig,
    val ticket: TicketConfig,
    val build: Map<String, BuildConfig>,
    val baseDir: File,
    val logDir: File,
    val include: Set<String>,
    val exclude: Set<String>,
    val args: Map<String, List<String>>,
    val command: String
) {

    fun isActiveKey(key: String) = (include.isEmpty() || include.contains(key))
        && (exclude.isEmpty() || !exclude.contains(key))

    val commonArgs = args[""] ?: emptyList()
    val activeArgs = projects.filterKeys(::isActiveKey).mapValues { args[it.key] ?: commonArgs }

    companion object {

        @JvmStatic
        fun parse(args: Array<String>, yamlLoader: (File) -> Config = Config.Companion::parseYaml): MpbConfig {
            val argState = Config.parseArgs(args)
            val configFile = (argState.get("config").file ?: File("mpb.yaml")).absoluteFile
            val configState = yamlLoader(configFile)
            argState.value?.also { configState.merge(it) }
            return MpbConfigConverter.config(configFile, configState)
        }

    }

}

data class BuildConfig(
    val profiles: Map<String, List<String>>,
    val env: Map<String, String>
)

data class JiraConfig(
    val url: String,
    val project: String
)

data class TicketConfig(
    val dir: File,
    val overwrite: Boolean
)

data class BranchConfig(
    val default: String?,
    val patterns: List<BranchPattern>
)

data class BranchPattern(
    val input: Regex,
    val branch: String,
    val index: Int
) {

    companion object {
        val REPLACE_ESCAPE_REGEX = Regex("(\\\\|\\\$(?!\\d|\\{.+}))")
        fun escapeReplacement(replacement: String): String {
            return REPLACE_ESCAPE_REGEX.replace(replacement, "\\\\$1")
        }
    }

    fun findBranch(input: String, list: List<String>): String? {
        val escapedBranch = escapeReplacement(branch)
        val replacedBranch = this.input.replace(input, escapedBranch)
        val branchRegex = Regex(replacedBranch)
        val matches = list
            .mapNotNull { branchRegex.matchEntire(it) }
            .map { it.groupValues.getOrNull(1) ?: it.value }

        return when {
            index < 0 && matches.isNotEmpty() -> return matches.last()
            index >= 0 && index < matches.size -> return matches[index]
            else -> null
        }
    }
}

data class ProjectConfig(
    val dir: File,
    val deps: Set<String>,
    val build: String,
    val branch: BranchConfig,
    val logFile: File
)

data class OutputConfig(
    val plain: Boolean,
    val monochrome: Boolean,
    val width: Int
) {
    fun withAnsiSupport(ansiUnsupported: Boolean) = OutputConfig(
        plain || ansiUnsupported,
        monochrome || ansiUnsupported,
        width
    )

    fun getWidth(providedWidth: Int): Int {
        return if (providedWidth == 0) width else providedWidth
    }
}
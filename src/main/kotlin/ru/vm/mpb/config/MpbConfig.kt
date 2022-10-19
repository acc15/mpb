package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config
import java.io.File

const val DEFAULT_KEY = "default"

data class MpbConfig(
    val config: File,
    val cd: File,
    val debug: Boolean,
    val branch: BranchConfig,
    val projects: Map<String, ProjectConfig>,
    val jira: JiraConfig,
    val ticket: TicketConfig,
    val build: Map<String, BuildConfig>,
    val baseDir: File,
    val include: Set<String>,
    val exclude: Set<String>,
    val args: Map<String, List<String>>,
    val command: String
) {

    val commonArgs = args[""] ?: emptyList()
    val activeArgs = projects.filterKeys {
        (include.isEmpty() || include.contains(it)) && (exclude.isEmpty() || !exclude.contains(it))
    }.mapValues { args[it.key] ?: commonArgs }

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
    val filters: List<BranchFilter>
)

data class BranchFilter(
    val regex: String,
    val index: Int
)

data class ProjectConfig(
    val dir: File,
    val deps: Set<String>,
    val build: String,
    val branch: BranchConfig
)


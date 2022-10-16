package ru.vm.mpb.config

import ru.vm.mpb.config.state.ConfigMutableMapState
import ru.vm.mpb.config.state.loadConfigYaml
import ru.vm.mpb.config.state.parseConfigArgs
import java.io.File

const val DEFAULT_KEY = "default"

data class MpbConfig(
    val config: File,
    val debug: Boolean,
    val defaultBranch: String,
    val branchPatterns: List<BranchPattern>,
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
    fun getDefaultBranch(proj: String) = projects[proj]?.defaultBranch ?: defaultBranch

    val commonArgs = args[""] ?: emptyList()
    val activeArgs = projects.filterKeys {
        (include.isEmpty() || include.contains(it)) && (exclude.isEmpty() || !exclude.contains(it))
    }.mapValues { args[it.key] ?: commonArgs }

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

data class BranchPattern(
    val pattern: String,
    val index: Int
)

data class ProjectConfig(
    val dir: File,
    val deps: Set<String>,
    val build: String,
    val defaultBranch: String?,
    val branchPatterns: List<BranchPattern>
)

fun parseArgsAndLoadConfig(args: List<String>, yamlLoader: (File) -> ConfigMutableMapState = ::loadConfigYaml): MpbConfig {
    val argState = parseConfigArgs(args)
    val configFile = (argState.get("config").file ?: File("mpb.yaml")).absoluteFile
    val configState = yamlLoader(configFile)
    configState.merge(argState.map)
    return ConfigConverter.config(configFile, configState)
}

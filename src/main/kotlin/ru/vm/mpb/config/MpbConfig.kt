package ru.vm.mpb.config

import java.io.File

const val DEFAULT_KEY = "default"

data class MpbConfig(
    val config: File,
    val debug: Boolean,
    val defaultBranch: String,
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
    fun getCommonArgs() = args[""] ?: emptyList()
    fun getActiveProjectArgs(): Map<String, List<String>> = projects.filterKeys {
        (include.isEmpty() || include.contains(it)) && (exclude.isEmpty() || !exclude.contains(it))
    }.mapValues {  args[it.key] ?: getCommonArgs() }
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

data class ProjectConfig(
    val dir: File,
    val deps: Set<String>,
    val build: String,
    val defaultBranch: String?
)

fun parseConfig(args: List<String>): MpbConfig {
    val argMap = ConfigMap.parseArgs(args)
    val cfgPath = (argMap.getFile("config") ?: File("mpb.yaml")).absoluteFile
    val configMap = ConfigMap.loadYaml(cfgPath)
    val mergedConfigMap = configMap.merge(argMap)
    return ConfigConverter.config(cfgPath, mergedConfigMap)
}
package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config
import java.io.File

const val DEFAULT_KEY = "default"

data class MpbConfig(
    val name: String,
    val debug: Boolean,
    val path: PathConfig,
    val output: OutputConfig,
    val branch: BranchConfig,
    val projects: Map<String, ProjectConfig>, // TODO rename to modules
    val jira: JiraConfig,
    val ticket: TicketConfig,
    val build: Map<String, BuildConfig>,
    val includes: IncludeExclude<String>,
    val args: ArgConfig
    // TODO add project: String, (default project to operate on, may be overriden by command-line)
    // TODO add projects: Map<String, MpbConfig>
) {

    companion object {

        @JvmStatic
        fun parse(list: Array<String>, yamlLoader: (File) -> Config = Config.Companion::parseYaml): MpbConfig {
            val args = Config.parseArgs(list)
            val file = (args.get("config").file ?: File("mpb.yaml")).absoluteFile
            return fromConfig(
                if (file.exists()) Config.mergeAll(listOf(yamlLoader(file).value, args.value)) else args,
                file
            )
        }

        fun fromConfig(cfg: Config, file: File): MpbConfig {
            val path = PathConfig.fromConfig(cfg, file)
            val build = cfg.get("build")
            val projects = cfg.get("projects").configMap.mapValues { (k, c) ->
                ProjectConfig.fromConfig(k, c, path, build)
            }
            val filters = IncludeExclude.fromConfig(cfg)

            return MpbConfig(
                cfg.get("name").string ?: "mpb",
                cfg.get("debug").flag,
                path,
                OutputConfig.fromConfig(cfg.get("output")),
                BranchConfig.fromConfig(cfg.get("branch")),
                projects,
                JiraConfig.fromConfig(cfg.get("jira")),
                TicketConfig.fromConfig(cfg.get("ticket"), path),
                build.configMap.mapValues { (_, c) -> BuildConfig.fromConfig(c, build, null) },
                filters,
                ArgConfig.fromConfig(cfg, projects.keys, filters)
            )
        }

    }

}


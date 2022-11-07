package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config
import ru.vm.mpb.config.state.ConfigRoot
import java.io.File

const val DEFAULT_KEY = "default"

data class MpbConfig(
    val name: String,
    val debug: Boolean,
    val path: PathConfig,
    val output: OutputConfig,
    val branch: BranchConfig,
    val projects: Map<String, ProjectConfig>,
    val jira: JiraConfig,
    val ticket: TicketConfig,
    val build: Map<String, BuildConfig>,
    val includes: IncludeExclude<String>,
    val args: ArgConfig,
    val profiles: List<String>
) {

    companion object {

        fun parse(list: Array<String>, yamlLoader: (File) -> Config = Config.Companion::parseYaml): MpbConfig {
            val args = Config.parseArgs(list)
            val file = baseDir(args).resolve(args.get("config").file ?: File("mpb.yaml"))

            val merged = ConfigRoot()
            if (file.exists()) {
                merged.merge(yamlLoader(file).value)
            }
            merged.merge(args.value)

            return fromConfig(merged)
        }

        fun baseDir(cfg: Config) = cfg.get("base").file ?: File("").absoluteFile

        fun fromConfig(cfg: Config): MpbConfig {
            val base = baseDir(cfg)
            val path = PathConfig.fromConfig(cfg, base)
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
                TicketConfig.fromConfig(cfg.get("ticket"), base),
                build.configMap.mapValues { (_, c) -> BuildConfig.fromConfig(c, build, null) },
                filters,
                ArgConfig.fromConfig(cfg, projects.keys, filters),
                cfg.get("profiles").map.keys.toList()
            )
        }

    }

}


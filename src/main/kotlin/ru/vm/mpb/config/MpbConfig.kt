package ru.vm.mpb.config

import ru.vm.mpb.config.loader.ConfigLoader
import ru.vm.mpb.config.state.Config

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
) {

    companion object {

        fun parse(vararg args: String) = fromConfig(ConfigLoader.load(*args))

        fun fromConfig(cfg: Config): MpbConfig {
            val base = cfg.get("base").path ?: MpbPath.home
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
                ArgConfig.fromConfig(cfg, projects.keys, filters))
        }

    }

}


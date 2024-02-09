package ru.vm.mpb.config

import org.fusesource.jansi.AnsiConsole
import ru.vm.mpb.config.loader.ConfigLoader
import ru.vm.mpb.config.state.Config

const val DEFAULT_KEY = "default"

data class MpbConfig(
    val name: String,
    val debug: Boolean,
    val seq: Boolean,
    val output: OutputConfig,
    val projects: Map<String, ProjectConfig>,
    val jira: JiraConfig,
    val ticket: TicketConfig,
    val build: Map<String, BuildConfig>,
    val args: ArgConfig,
    val profiles: Set<String>,
    val maxSessions: Int
) {

    companion object {

        fun parse(vararg args: String) = fromConfig(ConfigLoader(AnsiConsole.err()).load(*args).let { it.shorthand(it) })

        fun fromConfig(cfg: Config): MpbConfig {
            val path = PathConfig.fromConfig(cfg.get("path"))
            val projects = cfg.get("projects").configMap.mapValues { (k, c) ->
                ProjectConfig.fromConfig(k, c, cfg, path)
            }

            return MpbConfig(
                cfg.get("name").string ?: "mpb",
                cfg.get("debug").flag,
                cfg.get("seq").flag,
                OutputConfig.fromConfig(cfg.get("output")),
                projects,
                JiraConfig.fromConfig(cfg.get("jira")),
                TicketConfig.fromConfig(cfg.get("ticket"), path.base),
                cfg.get("build").configMap.mapValues { (_, c) ->
                    BuildConfig.fromConfig(BuildConfig.merge(cfg, c, null))
                },
                ArgConfig.fromConfig(cfg, projects.keys, IncludeExclude.fromConfig(cfg)),
                cfg.get("profiles").map.keys,
                cfg.get("git").shorthand.get("maxSessions").int ?: 0
            )
        }

    }

}


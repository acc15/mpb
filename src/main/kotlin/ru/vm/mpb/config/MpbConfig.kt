package ru.vm.mpb.config

import org.fusesource.jansi.Ansi
import org.fusesource.jansi.AnsiConsole
import ru.vm.mpb.config.state.Config
import ru.vm.mpb.config.state.ConfigRoot
import ru.vm.mpb.config.state.YamlLoader
import java.io.File
import java.io.FileNotFoundException

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

        val EMPTY = File("")

        fun baseDir(cfg: Config) = cfg.get("base").file ?: EMPTY

        fun parse(list: Array<String>): MpbConfig {
            val args = Config.parseArgs(list)
            val base = baseDir(args)
            val configs = args.get("config").files.ifEmpty { listOf(File("mpb.yaml")) }.map { base.resolve(it) }

            val merged = ConfigRoot()
            for (config in configs) {
                merged.merge(loadYamlIfExists(config))
            }
            merged.merge(args.value)
            return fromConfig(merged)
        }

        fun loadYamlIfExists(file: File): Any? {
            try {
                return YamlLoader.load(file)
            } catch (e: FileNotFoundException) {
                AnsiConsole.err().println(Ansi.ansi()
                    .fgBrightYellow().a("[warn] config file ${file.absoluteFile} doesn't exists").reset()
                )
                return null
            }
        }

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


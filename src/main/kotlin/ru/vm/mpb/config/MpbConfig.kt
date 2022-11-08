package ru.vm.mpb.config

import org.fusesource.jansi.Ansi
import org.fusesource.jansi.AnsiConsole
import ru.vm.mpb.config.state.Config
import ru.vm.mpb.config.state.ConfigRoot
import ru.vm.mpb.config.state.YamlLoader
import ru.vm.mpb.util.dfs
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
) {

    companion object {

        fun parse(list: Array<String>): MpbConfig {
            val args = Config.parseArgs(list)
            val cfg = ConfigRoot()
            val files = args.get("config").files.ifEmpty { listOf(File("mpb.yaml")) }.map { MpbPath.home.resolve(it) }
            mergeYamls(files, cfg)
            cfg.merge(args.value)
            return fromConfig(cfg)
        }

        fun mergeYamls(files: List<File>, dest: Config) {
            for (file in files) {
                val yaml = try {
                    YamlLoader.load(file)
                } catch (e: FileNotFoundException) {
                    AnsiConsole.err().println(Ansi.ansi()
                        .fgYellow().bold().a("[warn]").reset().a(" config file ${file.absoluteFile} doesn't exists")
                    )
                    continue
                }
                dest.merge(yaml)
            }
        }

        fun fromConfig(cfg: Config): MpbConfig {
            val base = cfg.get("base").file ?: MpbPath.home
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


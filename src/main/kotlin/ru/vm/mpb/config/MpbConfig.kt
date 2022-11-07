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
    val profiles: List<String>
) {

    companion object {

        fun baseDir(cfg: Config, defaultBase: File) = cfg.get("base").file ?: defaultBase
        fun resolveBase(base: File, files: List<File>) = files.map { base.resolve(it) }

        fun parse(list: Array<String>): MpbConfig {
            val args = Config.parseArgs(list)
            val cfg = ConfigRoot()

            mergeYamls(
                resolveBase(
                    baseDir(args, MpbPath.home),
                    args.get("config").files.ifEmpty { listOf(File("mpb.yaml")) }
                ),
                cfg
            )

            mergeProfiles(cfg, args.get("profile").stringSet)
            cfg.merge(args.value)

            return fromConfig(cfg)
        }

        fun mergeProfiles(cfg: Config, profiles: Iterable<String>) {
            for (profile in profiles) {

                val profileCfg = cfg.get("profiles").get(profile)
                val profileBase = baseDir(profileCfg, baseDir(cfg, MpbPath.home))
                val profileFiles = profileCfg.get("config").files.map { profileBase.resolve(it) }

                cfg.merge(profileCfg)
                mergeYamls(profileFiles, cfg)

            }
        }

        fun mergeYamls(files: List<File>, dest: Config) {
            dfs(files, { file ->

                val yaml = try {
                    YamlLoader.load(file)
                } catch (e: FileNotFoundException) {
                    AnsiConsole.err().println(Ansi.ansi()
                        .fgYellow().bold().a("[warn]").reset().a(" config file ${file.absoluteFile} doesn't exists")
                    )
                    return@dfs emptyList()
                }
                dest.merge(yaml)

                val cfg = Config.ofImmutable(yaml)
                resolveBase(baseDir(cfg, file.parentFile), cfg.get("config").files)

            }, onCycle = { cycle ->
                AnsiConsole.err().println(Ansi.ansi()
                    .fgRed().bold().a("[error]").reset().a(" config cycle detected $cycle")
                )
            })
        }

        fun fromConfig(cfg: Config): MpbConfig {
            val base = baseDir(cfg, MpbPath.home)
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


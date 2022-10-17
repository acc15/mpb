package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config
import java.io.File

object MpbConfigConverter {

    fun config(configFile: File, cfg: Config): MpbConfig {
        val baseDir = cfg.get("baseDir").file ?: configFile.parentFile
        return MpbConfig(
            configFile,
            cfg.get("debug").flag,
            branch(cfg.get("branch")),
            cfg.get("projects").configMap.mapValues { (k, c) -> project(k, baseDir, c) },
            jira(cfg.get("jira")),
            ticket(cfg.get("ticket"), baseDir),
            cfg.get("build").configMap.mapValues { (_, c) -> build(c) },
            baseDir,
            cfg.get("include").stringSet,
            cfg.get("exclude").stringSet,
            cfg.get("args").configMap.mapValues { (k, c) -> c.stringList.let { if (k.isNotEmpty()) it else it.drop(1) } },
            cfg.get("args").string.orEmpty()
        )
    }

    fun ticket(cfg: Config, baseDir: File) = TicketConfig(
        baseDir.resolve(cfg.get("dir").file ?: File("tickets")),
        cfg.get("overwrite").flag
    )

    fun jira(cfg: Config) = JiraConfig(
        cfg.get("url").string.orEmpty(),
        cfg.get("project").string.orEmpty()
    )

    fun project(key: String, baseDir: File, cfg: Config) = ProjectConfig(
        baseDir.resolve(cfg.get("dir").file ?: File(key)),
        cfg.get("deps").stringSet,
        cfg.get("build").string ?: DEFAULT_KEY,
        branch(cfg.get("branch"))
    )

    fun build(cfg: Config) = BuildConfig(
        cfg.get("profiles").configMap.mapValues { (_, c) -> c.stringList },
        cfg.get("env").configMap.mapValues { (_, c) -> c.string.orEmpty() }
    )

    fun branch(cfg: Config) = BranchConfig(
        cfg.get("default").string,
        cfg.get("filters").configList.map {
            BranchFilter(
                it.get("regex").string.orEmpty(),
                parseIndex(it.get("index").string.orEmpty()) ?: 0
            )
        }
    )

    fun parseIndex(idx: String): Int? = when {
        idx.equals("first", true) -> 0
        idx.equals("last", true) -> -1
        else -> idx.toIntOrNull()
    }

}


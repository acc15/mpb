package ru.vm.mpb.config

import ru.vm.mpb.config.state.ConfigState
import java.io.File

object ConfigConverter {

    fun config(configFile: File, cfg: ConfigState): MpbConfig {
        val baseDir = cfg.get("baseDir").file ?: configFile.parentFile
        return MpbConfig(
            configFile,
            cfg.get("debug").flag,
            cfg.get("defaultBranch").string ?: "master",
            convertList(cfg.get("branchPatterns"), ::branchPattern),
            convertMap(cfg.get("projects")) { c, k -> project(k, baseDir, c) },
            jira(cfg.get("jira")),
            ticket(baseDir, cfg.get("ticket")),
            convertMap(cfg.get("build")) { c, _ -> build(c) },
            baseDir,
            cfg.get("include").stringSet,
            cfg.get("exclude").stringSet,
            convertMap(cfg.get("args")) { c, k -> c.stringList.let { if (k.isNotEmpty()) it else it.drop(1) } },
            cfg.get("args").string.orEmpty()
        )
    }

    fun ticket(baseDir: File, cfg: ConfigState) = TicketConfig(
        baseDir.resolve(cfg.get("dir").file ?: File("tickets")),
        cfg.get("overwrite").flag
    )

    fun jira(cfg: ConfigState) = JiraConfig(
        cfg.get("url").string.orEmpty(),
        cfg.get("project").string.orEmpty()
    )

    fun project(key: String, baseDir: File, cfg: ConfigState) = ProjectConfig(
        baseDir.resolve(cfg.get("dir").file ?: File(key)),
        cfg.get("deps").stringSet,
        cfg.get("build").string ?: DEFAULT_KEY,
        cfg.get("defaultBranch").string,
        convertList(cfg.get("branchPatterns"), this::branchPattern)
    )

    fun build(cfg: ConfigState) = BuildConfig(
        convertMap(cfg.get("profiles")) { c, _ -> c.stringList },
        convertMap(cfg.get("env")) { c, _ -> c.string.orEmpty() }
    )

    fun branchPattern(cfg: ConfigState) = BranchPattern(
        cfg.get("pattern").string.orEmpty(),
        parseIndex(cfg.get("index").string.orEmpty()) ?: 0
    )

    fun parseIndex(idx: String): Int? = when {
        idx.equals("first", true) -> 0
        idx.equals("last", true) -> -1
        else -> idx.toIntOrNull()
    }

    fun <V> convertList(cfg: ConfigState, mapper: (ConfigState) -> V): List<V> = cfg.indices.map { mapper(cfg.get(it)) }
    fun <V> convertMap(cfg: ConfigState, mapper: (ConfigState, String) -> V): Map<String, V> = cfg.keys
        .associateWith { mapper(cfg.get(it), it) }

}


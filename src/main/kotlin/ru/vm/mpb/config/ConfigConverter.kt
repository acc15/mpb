package ru.vm.mpb.config

import java.io.File

object ConfigConverter {

    fun config(configFile: File, cfg: ConfigMap): MpbConfig {
        val baseDir = cfg.getFile("baseDir") ?: configFile.parentFile
        return MpbConfig(
            configFile,
            cfg.getBoolean("debug"),
            cfg.getString("defaultBranch") ?: "master",
            map(cfg.getConfig("projects")) { c, k -> project(k, baseDir, c.getConfig(k)) },
            jira(cfg.getConfig("jira")),
            ticket(baseDir, cfg.getConfig("ticket")),
            map(cfg.getConfig("build")) { c, k -> build(c.getConfig(k)) },
            baseDir,
            cfg.getStringSet("include"),
            cfg.getStringSet("exclude"),
            map(cfg.getConfig("args")) { c, k ->
                c.getStringList(k).orEmpty().let {
                    if (it.isEmpty() || k.isNotEmpty()) it else it.subList(1, it.size)
                }
            },
            cfg.getString("args").orEmpty()
        )
    }

    fun ticket(baseDir: File, cfg: ConfigMap) = TicketConfig(
        baseDir.resolve(cfg.getFile("dir") ?: File("tickets")),
        cfg.getBoolean("overwrite")
    )

    fun jira(cfg: ConfigMap) = JiraConfig(
        cfg.getString("url").orEmpty(),
        cfg.getString("project").orEmpty()
    )

    fun project(key: String, baseDir: File, cfg: ConfigMap) = ProjectConfig(
        baseDir.resolve(cfg.getFile("dir") ?: File(key)),
        cfg.getStringSet("deps"),
        cfg.getString("build") ?: DEFAULT_KEY,
        cfg.getString("defaultBranch")
    )

    fun build(cfg: ConfigMap) = BuildConfig(
        map(cfg.getConfig("profiles")) { c, k -> c.getStringList(k).orEmpty() },
        map(cfg.getConfig("env")) { c, k -> c.getString(k).orEmpty() }
    )

    fun <V> map(cfg: ConfigMap, valueConverter: (ConfigMap, String) -> V): Map<String, V> = cfg.map.mapValues {
        valueConverter(cfg, it.key)
    }

}


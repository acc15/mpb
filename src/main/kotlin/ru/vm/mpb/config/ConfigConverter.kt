package ru.vm.mpb.config

import java.io.File

fun convertConfig(configFile: File, cfg: ConfigValues): MpbConfig {
    val baseDir = cfg.getFile("baseDir") ?: configFile.parentFile
    return MpbConfig(
        configFile,
        cfg.getBoolean("debug"),
        cfg.getString("defaultBranch") ?: "master",
        convertMap(cfg.getConfig("projects")) { c, k -> convertProject(k, baseDir, c.getConfig(k)) },
        convertJira(cfg.getConfig("jira")),
        convertTicket(baseDir, cfg.getConfig("ticket")),
        convertMap(cfg.getConfig("build")) { c, k -> convertBuild(c.getConfig(k)) },
        baseDir,
        cfg.getStringSet("include"),
        cfg.getStringSet("exclude"),
        convertMap(cfg.getConfig("args")) { c, k ->
            c.getStringList(k).orEmpty().let {
                if (it.isEmpty() || k.isNotEmpty()) it else it.subList(1, it.size)
            }
        },
        cfg.getString("args").orEmpty()
    )
}

fun convertTicket(baseDir: File, cfg: ConfigValues) = TicketConfig(
    baseDir.resolve(cfg.getFile("dir") ?: File("tickets")),
    cfg.getBoolean("overwrite")
)

fun convertJira(cfg: ConfigValues) = JiraConfig(
    cfg.getString("url").orEmpty(),
    cfg.getString("project").orEmpty()
)

fun convertProject(key: String, baseDir: File, cfg: ConfigValues) = ProjectConfig(
    baseDir.resolve(cfg.getFile("dir") ?: File(key)),
    cfg.getStringSet("deps"),
    cfg.getString("build") ?: "default",
    cfg.getString("defaultBranch")
)

fun convertBuild(cfg: ConfigValues) = BuildConfig(
    convertMap(cfg.getConfig("profiles")) { c, k -> c.getStringList(k).orEmpty() },
    convertMap(cfg.getConfig("env")) { c, k -> c.getString(k).orEmpty() }
)

fun <V> convertMap(cfg: ConfigValues, valueConverter: (ConfigValues, String) -> V): Map<String, V> = cfg.map.mapValues {
    valueConverter(cfg, it.key)
}

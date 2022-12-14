package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config
import java.util.*

data class BuildConfig(
    val use: String?,
    val tool: List<String>,
    val opt: List<String>,
    val commands: Map<String, List<String>>,
    val env: Map<String, String>,
    val progress: ProgressConfig
) {

    fun getCommand(command: String?) = command?.let { commands[it] } ?: commands.getValue(DEFAULT_KEY)
    fun getCommandLine(command: String?) = tool + opt + getCommand(command)

    companion object {

        fun merge(rootBuild: Config, projectBuild: Config, defaultUse: String?): Config {
            val initKey = projectBuild.get("use").string ?: defaultUse ?: return projectBuild

            val values = LinkedList<Config>()
            values.addFirst(projectBuild)

            var k: String? = initKey
            while (k != null) {
                val b = rootBuild.get(k)
                values.addFirst(b)
                k = b.get("use").string
                if (k == initKey) {
                    break
                }
            }

            val merged = Config.mergeAll(values)
            merged.get("use").set(initKey)
            return merged
        }

        fun fromConfig(cfg: Config) = BuildConfig(
            cfg.get("use").string,
            cfg.get("tool").stringList,
            cfg.get("opt").stringList,
            cfg.get("commands").configMap.mapValues { (_, c) -> c.stringList },
            cfg.get("env").configMap.mapValues { (_, c) -> c.string.orEmpty() },
            ProgressConfig.fromConfig(cfg.get("progress"))
        )
    }
}
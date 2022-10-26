package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config
import java.util.*

data class BuildConfig(
    val use: String?,
    val cmd: List<String>,
    val opt: List<String>,
    val profiles: Map<String, List<String>>,
    val env: Map<String, String>
) {

    fun getProfile(profile: String?) = profile?.let { profiles[it] } ?: profiles.getValue(DEFAULT_KEY)
    fun makeCommand(profile: String?) = cmd + opt + getProfile(profile)

    companion object {

        private fun mergeBuild(cfg: Config, build: Config, defaultUse: String?): Config {
            val initKey = cfg.get("use").string ?: defaultUse ?: return cfg

            val configs = LinkedList<Config>()
            configs.addFirst(cfg)

            var k: String? = initKey
            while (k != null) {
                val b = build.get(k)
                configs.addFirst(b)
                k = b.get("use").string
                if (k == initKey) {
                    break
                }
            }
            return Config.mergeAll(configs)
        }

        fun fromConfig(cfg: Config, build: Config, defaultUse: String?) = mergeBuild(cfg, build, defaultUse).let {
            BuildConfig(
                it.get("use").string ?: defaultUse,
                it.get("cmd").stringList,
                it.get("opt").stringList,
                it.get("profiles").configMap.mapValues { (_, c) -> c.stringList },
                it.get("env").configMap.mapValues { (_, c) -> c.string.orEmpty() }
            )
        }
    }
}
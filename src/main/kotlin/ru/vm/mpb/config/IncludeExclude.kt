package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config

data class IncludeExclude<K>(
    val include: Set<K>,
    val exclude: Set<K>
) {
    fun includes(key: K): Boolean {
        return (include.isEmpty() || include.contains(key)) && (exclude.isEmpty() || !exclude.contains(key))
    }

    companion object {
        fun fromConfig(cfg: Config) = IncludeExclude(
            cfg.get("include").stringSet,
            cfg.get("exclude").stringSet
        )
    }
}

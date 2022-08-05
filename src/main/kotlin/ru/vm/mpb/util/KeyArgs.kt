package ru.vm.mpb.util

import ru.vm.mpb.config.MpbConfig

data class KeyArgs(val map: Map<String?, List<String>>) {
    operator fun get(key: String): List<String> = map[key] ?: map[null]!!
}

fun parseKeyArgs(keyPrefix: String, keyFilter: (String) -> Boolean, args: List<String>): KeyArgs {
    val map = HashMap<String?, MutableList<String>>()
    map[null] = ArrayList()

    var currentKey: String? = null
    for (a in args) {
        if (a.startsWith(keyPrefix)) {
            val key = a.substring(keyPrefix.length)
            if (keyFilter(key)) {
                currentKey = key
                continue
            }
        }
        map.computeIfAbsent(currentKey) { ArrayList() }.add(a)
    }
    return KeyArgs(map)
}

fun parseProjectArgs(cfg: MpbConfig, args: List<String>) = parseKeyArgs("--", cfg.projects::containsKey, args)
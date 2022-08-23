package ru.vm.mpb.util

import ru.vm.mpb.config.MpbConfig

typealias KeyArgs = Map<String, List<String>>

const val KEY_PREFIX = "--"
const val INCLUDE_PREFIX = "+"
const val EXCLUDE_PREFIX = "-"

fun parseKeyArgs(cfg: MpbConfig, args: List<String>): KeyArgs {
    val map = HashMap<String?, MutableList<String>>()
    var currentKeys: Set<String> = cfg.projects.keys
    val includes = mutableSetOf<String>()
    val excludes = mutableSetOf<String>()

    for (a in args) {
        runAfterPrefix(a, KEY_PREFIX, cfg, true) { currentKeys = it.ifEmpty { cfg.projects.keys } } ?: continue
        if (currentKeys.isEmpty()) {
            runAfterPrefix(a, INCLUDE_PREFIX, cfg) { includes.addAll(it) } ?: continue
            runAfterPrefix(a, EXCLUDE_PREFIX, cfg) { excludes.addAll(it) } ?: continue
        }
        for (k in currentKeys) {
            map.computeIfAbsent(k) { ArrayList() }.add(a)
        }
    }

    val result = mutableMapOf<String, List<String>>()
    for (p in cfg.projects.keys) {
        if (includes.isNotEmpty() && !includes.contains(p)) {
            continue
        }
        if (excludes.isNotEmpty() && excludes.contains(p)) {
            continue
        }
        result[p] = map[p] ?: map[null] ?: emptyList()
    }
    return result
}

fun runAfterPrefix(a: String, prefix: String, cfg: MpbConfig, allowEmpty: Boolean = false, fn: (Set<String>) -> Unit): Unit? {
    if (!a.startsWith(prefix)) {
        return Unit
    }
    val k = a.substring(prefix.length)
    val keys = k.split(",").filter { cfg.projects.containsKey(it) }.toSet()
    if (keys.isEmpty() && !allowEmpty) {
        return Unit
    }
    fn(keys)
    return null
}



package ru.vm.mpb.util

import ru.vm.mpb.config.MpbConfig

typealias KeyArgs = Map<String, List<String>>

const val KEY_PREFIX = "--"
const val INCLUDE_PREFIX = "+"
const val EXCLUDE_PREFIX = "-"

fun parseKeyArgs(cfg: MpbConfig, args: List<String>): KeyArgs {
    val map = mutableMapOf<String, MutableList<String>>()
    var currentKeys: Set<String> = emptySet()
    val includes = mutableSetOf<String>()
    val excludes = mutableSetOf<String>()

    for (a in args) {
        if (runAfterPrefix(a, KEY_PREFIX, cfg, true) { currentKeys = it }) {
            continue
        }
        if (currentKeys.isEmpty()) {
            if (runAfterPrefix(a, INCLUDE_PREFIX, cfg) { includes.addAll(it) }) {
                continue
            }
            if (runAfterPrefix(a, EXCLUDE_PREFIX, cfg) { excludes.addAll(it) }) {
                continue
            }
        }
        for (k in currentKeys.ifEmpty { cfg.projects.keys }) {
            map.computeIfAbsent(k) { ArrayList() }.add(a)
        }
    }
    return cfg.projects.keys
        .filter { includes.isEmpty() || includes.contains(it) }
        .filter { excludes.isEmpty() || !excludes.contains(it) }
        .associateWith { (map[it] ?: emptyList()) }
}

fun runAfterPrefix(a: String, prefix: String, cfg: MpbConfig, allowEmpty: Boolean = false, fn: (Set<String>) -> Unit): Boolean {
    if (!a.startsWith(prefix)) {
        return false
    }
    val k = a.substring(prefix.length)
    val keys = k.split(",").filter { cfg.projects.containsKey(it) }.toSet()
    if (keys.isEmpty() && !allowEmpty) {
        return false
    }
    fn(keys)
    return true
}



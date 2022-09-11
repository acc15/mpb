package ru.vm.mpb.config

import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

const val OPT_PREFIX = "--"
const val KEY_DELIM = '.'

fun parseKeys(opt: String): List<String> = if (opt.isEmpty()) emptyList() else opt.split(KEY_DELIM)

@Suppress("UNCHECKED_CAST")
fun resolveList(m: MutableMap<String, Any>, k: String): MutableList<String> = when (val v = m[k]) {
    is MutableList<*> -> v as MutableList<String>
    is MutableMap<*, *> -> (v as MutableMap<String, Any>)
        .computeIfAbsent("") { mutableListOf<String>() } as MutableList<String>
    else -> mutableListOf<String>().also { m[k] = it }
}

fun parseArgs(args: List<String>): Map<String, Any> {

    var targetList = mutableListOf<String>()
    val result = mutableMapOf<String, Any>(
        "args" to mutableMapOf(
            "" to targetList
        )
    )

    for (a in args) {
        if (!a.startsWith(OPT_PREFIX)) {
            targetList += a
            continue
        }

        val keys = parseKeys(a.substring(OPT_PREFIX.length)).ifEmpty { listOf("args") }

        var map = result
        for (k in keys.subList(0, keys.size - 1)) {
            @Suppress("UNCHECKED_CAST")
            map = map.compute(k) { _, v ->
                when (v) {
                    is MutableList<*> -> mutableMapOf("" to v)
                    is MutableMap<*, *> -> v
                    else -> mutableMapOf<String, Any>()
                }
            } as MutableMap<String, Any>
        }

        val lastKey = keys.last()
        targetList = resolveList(map, lastKey)
    }

    return result
}

fun loadConfig(io: InputStream): Map<String, Any> = Yaml().load(io)
fun loadConfig(file: File): Map<String, Any> = FileInputStream(file).use {
    loadConfig(it)
}

fun mergeValues(list: List<Any>): Any {
    val hasMap = list.find { it is Map<*, *> } != null
    if (!hasMap) {
        return list.last().let { if (it is List<*>) it else listOf(it) }
    }

    val result = mutableMapOf<String, Any>()
    for (v in list) {
        @Suppress("UNCHECKED_CAST")
        when (v) {
            is Map<*, *> -> mergeMaps(listOf(result, v as Map<String, Any>), result)
            is List<*> -> result[""] = v
            else -> result[""] = listOf(v)
        }
    }
    return result
}

private fun mergeMaps(maps: List<Map<String, Any>>, target: MutableMap<String, Any>) {
    val keys = maps.flatMap { it.keys }.toSet()
    for (k in keys) {
        target[k] = mergeValues(maps.mapNotNull { it[k] })
    }
}

fun mergeConfigMaps(maps: List<Map<String, Any>>) = mutableMapOf<String, Any>().also { mergeMaps(maps, it) }

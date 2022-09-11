package ru.vm.mpb.config

import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

@Suppress("UNCHECKED_CAST")
class ConfigMap(val map: Map<String, Any>) {
    fun getConfig(key: String) = ConfigMap(
        map[key]?.let { it as? Map<String, Any> ?: (it as? List<String>)?.let { l -> mapOf("" to l) } }.orEmpty()
    )

    fun getStringList(key: String): List<String>? = map[key]?.let {
        var resolved: Any? = it
        if (resolved is Map<*, *>) {
            resolved = (resolved as Map<String, Any>)[""]
        }
        resolved as? List<String> ?: resolved?.let { listOf(resolved as String) }
    }

    fun getString(key: String): String? = map[key]?.let {
        var resolved: Any? = it
        if (resolved is Map<*, *>) {
            resolved = (resolved as Map<String, Any>)[""]
        }
        if (resolved is List<*>) {
            resolved = (resolved as List<String>).firstOrNull()
        }
        resolved as? String
    }

    fun getStringSet(key: String) = getStringList(key).orEmpty().toSet()
    fun getBoolean(key: String) = getStringList(key)?.let { it.isEmpty() || it.first().toBoolean() } ?: false
    fun getFile(key: String) = getString(key)?.let { File(it) }

    fun merge(other: ConfigMap) = ConfigMap(deepMerge(map, other.map))

    companion object {

        const val OPT_PREFIX = "--"
        const val KEY_DELIM = '.'

        private fun wrapList(v: Any) = v as? List<String> ?: listOf(v)

        private fun deepMerge(m1: Map<String, Any>, m2: Map<String, Any>): Map<String, Any> {
            val keys = m1.keys + m2.keys

            val result = mutableMapOf<String, Any>()
            for (k in keys) {

                val v1 = m1[k]
                val v2 = m2[k]

                result[k] = when {
                    v1 is Map<*, *> && v2 is Map<*, *> -> deepMerge(v1 as Map<String, Any>, v2 as Map<String, Any>)
                    v1 is Map<*, *> && v2 != null -> v1 + ("" to wrapList(v2))
                    v1 is Map<*, *> -> v1
                    v1 != null && v2 is Map<*, *> && !v2.containsKey("") -> v2 + ("" to wrapList(v1))
                    else -> (v2 ?: v1)!!
                }

            }
            return result
        }


        fun parseKeys(opt: String): List<String> = if (opt.isEmpty()) emptyList() else opt.split(KEY_DELIM)

        private fun getValueList(keyPath: String, result: MutableMap<String, Any>): MutableList<String> {

            val keys = parseKeys(keyPath).ifEmpty { listOf("args") }

            var map = result
            for (k in keys.subList(0, keys.size - 1)) {
                map = map.compute(k) { _, v ->
                    when (v) {
                        is MutableList<*> -> mutableMapOf("" to v)
                        is MutableMap<*, *> -> v
                        else -> mutableMapOf<String, Any>()
                    }
                } as MutableMap<String, Any>
            }

            val lastKey = keys.last()

            return when (val v = map[lastKey]) {
                is MutableList<*> -> v as MutableList<String>
                is MutableMap<*, *> -> (v as MutableMap<String, Any>)
                    .computeIfAbsent("") { mutableListOf<String>() } as MutableList<String>
                else -> mutableListOf<String>().also { map[lastKey] = it }
            }
        }

        fun parseArgs(args: List<String>): ConfigMap {
            var targetList: MutableList<String>? = null
            val result = mutableMapOf<String, Any>()
            for (a in args) {
                val isOpt = a.startsWith(OPT_PREFIX)
                if (targetList == null || isOpt) {
                    targetList = getValueList(if (isOpt) a.substring(OPT_PREFIX.length) else "", result)
                }
                if (!isOpt) {
                    targetList += a
                }
            }
            return ConfigMap(result)
        }

        fun loadYaml(io: InputStream) = ConfigMap(Yaml().load(io))

        fun loadYaml(file: File) = FileInputStream(file).use {
            loadYaml(it)
        }

    }

}
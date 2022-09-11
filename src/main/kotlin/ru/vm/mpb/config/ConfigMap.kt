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
        it as? List<String> ?: (it as? Map<String, Any>)?.get("") as? List<String>
    }

    fun getStringSet(key: String) = getStringList(key).orEmpty().toSet()
    fun getBoolean(key: String) = getStringList(key)?.let { it.isEmpty() || it.first().toBoolean() } ?: false

    fun <T> getValue(key: String, converter: (String) -> T) = getStringList(key)?.firstOrNull()?.let(converter)
    fun getString(key: String) = getValue(key) { it }
    fun getFile(key: String) = getValue(key) { File(it) }

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

                val m = if (v1 is Map<*, *>)
                    if (v2 is Map<*, *>)
                        deepMerge(v1 as Map<String, Any>, v2 as Map<String, Any>)
                    else if (v2 != null)
                        v1 + ("" to wrapList(v2))
                    else
                        v1
                else if (v1 != null && v2 is Map<*, *> && !v2.containsKey(""))
                    v2 + ("" to wrapList(v1))
                else (v2 ?: v1)!!

                result[k] = m
            }
            return result
        }


        fun parseKeys(opt: String): List<String> = if (opt.isEmpty()) emptyList() else opt.split(KEY_DELIM)

        fun getValueList(keyPath: String, result: MutableMap<String, Any>): MutableList<String> {
            val keys = parseKeys(keyPath).ifEmpty { listOf("args") }

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

            @Suppress("UNCHECKED_CAST")
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
                if (!a.startsWith(OPT_PREFIX)) {
                    if (targetList == null) {
                        targetList = getValueList("", result)
                    }
                    targetList += a
                    continue
                }
                targetList = getValueList(a.substring(OPT_PREFIX.length), result)
            }
            return ConfigMap(result)
        }

        fun loadYaml(io: InputStream) = ConfigMap(Yaml().load(io))

        fun loadYaml(file: File) = FileInputStream(file).use {
            loadYaml(it)
        }

    }

}
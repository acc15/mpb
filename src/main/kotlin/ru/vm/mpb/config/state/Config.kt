package ru.vm.mpb.config.state

import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileReader
import java.io.InputStream
import java.io.Reader
import java.lang.StringBuilder
import java.lang.UnsupportedOperationException
import java.net.URL

/*
 *  State mutation rules:
 *
 *  plain - String, Int, Boolean, etc
 *  plain(null).add -> plain(value).add -> list(value, anotherValue)
 *  plain.get(index).add -> new list with (0 to oldValue, index to value)
 *  plain.get(key).add -> new map with ("" to oldValue, key to value)
 *
 *  list - ListState
 *  list.add -> list(...oldValues, value)
 *  list.get(index).add -> list(...oldValues, index to toState(oldValue).add(value))
 *  list.get(key).add -> map("" to list, key to value)
 *
 *  map - MapState
 *  map.add -> map(...entries, "" to toState(oldValue).add(value))
 *  map.get(index).add -> map(...entries, "" to toState(oldValue).get(index).add(value))
 *  map.get(key).add -> map(...entries key to toState(oldValue).add(value))
 *
 */

typealias ConfigMutator = (Any?) -> Unit

abstract class Config(private val mutator: ConfigMutator) {

    // State management

    abstract val value: Any?
    abstract fun get(key: String): Config
    abstract fun get(index: Int): Config
    abstract fun add(value: Any?)
    open fun set(value: Any?) {
        mutator(value)
    }

    open fun merge(value: Any?) {
        mapValueByType(value,
            { map -> ConfigMap(LinkedHashMap<String, Any>().also(mutator), this::set).merge(map) },
            { list -> ConfigList(ArrayList<Any?>().also(mutator), this::set).merge(list) },
            { plain -> set(plain) },
            {})
    }

    // Path functions

    fun path(p: String) = path(parsePath(p))
    fun path(p: List<Any>) = p.fold(this) { s, k -> if (k is Int) s.get(k) else s.get(k as String) }

    // Converting functions

    abstract val list: List<Any?>
    abstract val map: Map<String, Any>
    abstract val plain: Any?
    val string: String? get() = plain?.toString()
    val stringList: List<String> get() = list.mapNotNull { ofImmutable(it).string }
    val stringSet: Set<String> get() = stringList.toSet()
    val int: Int? get() = plain?.let {
        (it as? Boolean)?.let { b -> if (b) 1 else 0 }
        (it as? String)?.toIntOrNull()
    }

    val flag: Boolean get() = plain?.let {
        it as? Boolean ?:
        (it as? String)?.toBoolean()
    } ?: false
    val file: File? get() = string?.let { File(it) }
    val configList: List<Config> get() = list.map { ofImmutable(it) }
    val configMap: Map<String, Config> get() = map.mapValues { ofImmutable(it.value) }

    companion object {

        fun ofImmutable(value: Any?) = of(value) { throw UnsupportedOperationException() }

        fun of(value: Any?, mutator: ConfigMutator) = mapValueByType(value,
            { map -> ConfigMap(map, mutator) },
            { list -> ConfigList(list, mutator) },
            { plain -> ConfigPlain(plain, mutator) },
            { ConfigNull(mutator) }
        )

        fun parsePath(str: String): List<Any> {

            val segments = mutableListOf<Any>()
            val segment = StringBuilder()
            var indexDepth = 0

            fun flush(allowEmpty: Boolean) {
                if (segment.isEmpty() && !allowEmpty) {
                    return
                }
                segments.add(segment.toString().let {
                    if (indexDepth == 1) (it.trim().toIntOrNull() ?: it) else it
                })
                segment.clear()
            }

            for ((i, ch) in str.withIndex()) {
                when {
                    indexDepth == 0 && ch == '.' -> {
                        flush(i == 0 || str[i - 1] != ']')
                        continue
                    }
                    indexDepth == 0 && ch == '[' -> {
                        flush(i > 0 && str[i - 1] == '.')
                        ++indexDepth
                        continue
                    }
                    indexDepth > 0 && ch == '[' -> {
                        ++indexDepth
                    }
                    indexDepth > 1 && ch == ']' -> {
                        --indexDepth
                    }
                    indexDepth == 1 && ch == ']' -> {
                        flush(true)
                        indexDepth = 0
                        continue
                    }
                }
                segment.append(ch)
            }
            flush(false)

            return segments
        }

        fun parseArgs(args: Array<String>): Config {
            val optPrefix = "--"
            val defaultPath = parsePath("args")
            val state = ConfigMap(mutableMapOf()) {}

            val values = mutableListOf<String>()
            var path = defaultPath

            fun flush() {
                if (values.isEmpty()) {
                    if (path != defaultPath) {
                        state.path(path).set(true)
                    }
                    return
                }
                for (v in values) {
                    state.path(path).add(v)
                }
                values.clear()
            }

            for (a in args) {
                if (a.startsWith(optPrefix)) {
                    flush()
                    path = parsePath(a.substring(optPrefix.length)).ifEmpty { defaultPath }
                    continue
                }
                values.add(a)
            }
            flush()
            return state
        }

        fun parseYaml(loader: (Yaml) -> Map<String, Any>): Config = ConfigMap(loader(Yaml()).toMutableMap()) {}
        fun parseYaml(stream: InputStream) = parseYaml { yaml -> yaml.load(stream) }
        fun parseYaml(reader: Reader) = parseYaml { yaml -> yaml.load(reader) }
        fun parseYaml(file: File) = FileReader(file).use { parseYaml(it) }
        fun parseYaml(url: URL) = url.openStream().use { parseYaml(it) }

        @Suppress("UNCHECKED_CAST")
        fun <T> mapValueByType(
            value: Any?,
            mapHandler: (Map<String, Any>) -> T,
            listHandler: (List<Any?>) -> T,
            plainHandler: (Any) -> T,
            nullHandler: () -> T
        ): T = when (value) {
            is Map<*, *> -> mapHandler(value as Map<String, Any>)
            is List<*> -> listHandler(value as List<Any?>)
            null -> nullHandler()
            else -> plainHandler(value)
        }

        fun putNonNull(list: MutableList<Any?>, vararg values: Pair<Int, Any?>): MutableList<Any?> {
            for (v in values) {
                if (v.second == null) {
                    continue
                }
                while (list.size <= v.first) {
                    list.add(null)
                }
                list[v.first] = v.second
            }
            return list
        }

        fun putNonNull(map: MutableMap<String, Any>, vararg values: Pair<String, Any?>): MutableMap<String, Any> {
            for (v in values) {
                map[v.first] = v.second ?: continue
            }
            return map
        }

        fun mergeAll(configs: List<Config>): Config {
            val result = ConfigRoot()
            for (c in configs) {
                result.merge(c.value)
            }
            return result.state
        }

    }

}

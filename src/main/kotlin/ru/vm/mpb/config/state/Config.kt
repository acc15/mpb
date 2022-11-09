package ru.vm.mpb.config.state

import java.io.File
import java.lang.UnsupportedOperationException
import java.nio.file.Path
import kotlin.io.path.Path

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
    abstract fun add(other: Any?)
    abstract fun merge(other: Any?)
    open fun set(other: Any?) {
        mutator(other)
    }

    fun remove() = set(null)

    // Path functions

    fun path(p: String) = path(ConfigPath.parse(p))
    fun path(p: List<Any>) = p.fold(this) { s, k -> if (k is Int) s.get(k) else s.get(k as String) }

    // Converting functions
    abstract val list: List<Any?>
    abstract val map: Map<String, Any>
    abstract val plain: Any?

    val configList: List<Config> get() = list.map { ofImmutable(it) }
    val configMap: Map<String, Config> get() = map.mapValues { ofImmutable(it.value) }

    val string: String? get() = plain?.toString()
    val stringList: List<String> get() = list.mapNotNull { ofImmutable(it).string }
    val stringSet: Set<String> get() = LinkedHashSet(stringList)
    val int: Int? get() = plain?.let {
        (it as? Boolean)?.let { b -> if (b) 1 else 0 }
        (it as? String)?.toIntOrNull()
    }

    val flag: Boolean get() = plain?.let { it as? Boolean ?: (it as? String)?.toBoolean() } ?: false

    val file: File? get() = string?.let(::File)
    val files: List<File> get() = stringList.map(::File)

    val path: Path? get() = string?.let(::Path)
    val paths: List<Path> get() = stringList.map(::Path)

    companion object {

        val immutable: ConfigMutator = {
            throw UnsupportedOperationException("Mutation is not allowed for immutable Config objects")
        }

        fun ofImmutable(value: Any?) = of(value, immutable)

        fun of(value: Any?, mutator: ConfigMutator) = mapByType(value,
            { map -> ConfigMap(map, mutator) },
            { list -> ConfigList(list, mutator) },
            { plain -> ConfigPlain(plain, mutator) }
        )

        fun parseArgs(vararg args: String): Config {
            val optPrefix = "--"
            val defaultPath = ConfigPath.parse("args")
            val state = ConfigMap(mutableMapOf()) {}

            val values = mutableListOf<String>()
            var path = defaultPath

            fun flush() {
                if (values.isEmpty() && path != defaultPath) {
                    state.path(path).set(true)
                }
                for (v in values) {
                    state.path(path).add(v)
                }
                values.clear()
            }

            for (a in args) {
                if (a.startsWith(optPrefix)) {
                    flush()
                    path = ConfigPath.parse(a.substring(optPrefix.length)).ifEmpty { defaultPath }
                    continue
                }
                values.add(a)
            }
            flush()
            return state
        }

        @Suppress("UNCHECKED_CAST")
        fun <T> mapByType(
            value: Any?,
            mapHandler: (Map<String, Any>) -> T,
            listHandler: (List<Any?>) -> T,
            plainHandler: (Any?) -> T,
        ): T = when (value) {
            is Map<*, *> -> mapHandler(value as Map<String, Any>)
            is List<*> -> listHandler(value as List<Any?>)
            else -> plainHandler(value)
        }

        fun isPlain(value: Any?) = mapByType(value, { false }, { false }, { true })
        fun isPlainList(list: List<Any?>) = list.all { isPlain(it) }

        fun <T: MutableList<Any?>> applyValues(list: T, vararg values: Pair<Int, Any?>): T {
            for (v in values) {
                if (v.first >= list.size && v.second == null) {
                    continue
                }
                while (list.size <= v.first) {
                    list.add(null)
                }
                list[v.first] = v.second
            }
            return list
        }

        fun <T: MutableMap<String, Any>> applyValues(map: T, vararg values: Pair<String, Any?>): T {
            for (v in values) {
                if (v.second != null) {
                    map[v.first] = v.second!!
                } else {
                    map.remove(v.first)
                }
            }
            return map
        }

        fun mergeAll(values: Iterable<Config>): Config {
            val result = ConfigRoot()
            for (v in values) {
                result.merge(v.value)
            }
            return result.state
        }

        fun mergeAll(vararg configs: Config) = mergeAll(configs.toList())

    }

}

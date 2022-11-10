package ru.vm.mpb.config.state

import java.io.File
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

interface Config {

    // Getters
    val value: Any?
    fun get(key: String): Config
    fun get(index: Int): Config

    // Mutators
    fun add(other: Any?)
    fun merge(other: Any?)
    fun set(other: Any?)
    fun remove() = set(null)

    // Path functions
    fun path(p: String) = path(ConfigPath.parse(p))
    fun path(p: List<Any>) = p.fold(this) { s, k -> if (k is Int) s.get(k) else s.get(k as String) }

    fun shorthand(shorthand: Config = this) = ConfigShorthand(this, ConfigComposite(listOf(shorthand, this)))

    val shorthand: Config get() = this

    // Converting functions
    val list: List<Any?>
    val map: Map<String, Any>
    val plain: Any?

    val configList: List<Config> get() = list.indices.map { get(it) }
    val configMap: Map<String, Config> get() = map.keys.associateWith { get(it) }

    val string: String? get() = plain?.toString()
    val stringList: List<String> get() = list.mapNotNull { immutable(it).string }
    val stringSet: Set<String> get() = LinkedHashSet(stringList)

    val int: Int? get() = plain?.let {
        when (it) {
            is Boolean -> if (it) 1 else 0
            is Number -> it.toInt()
            is String -> it.toIntOrNull()
            else -> null
        }
    }

    val bool: Boolean? get() = plain?.let {
        when (it) {
            is Boolean -> it
            is Number -> it.toInt() != 0
            is String -> it.toBoolean()
            else -> null
        }
    }

    val flag get() = bool ?: false

    val file: File? get() = string?.let(::File)
    val files: List<File> get() = stringList.map(::File)

    val path: Path? get() = string?.let(::Path)
    val paths: List<Path> get() = stringList.map(::Path)

    companion object {

        fun immutable(value: Any?) = of(value, immutable)

        fun of(value: Any?, mutator: ConfigMutator) = mapByType(value,
            { map -> ConfigMap(map, mutator) },
            { list -> ConfigList(list, mutator) },
            { plain -> ConfigPlain(plain, mutator) }
        )

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

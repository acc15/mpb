package ru.vm.mpb.config.state

import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileReader
import java.io.InputStream
import java.io.Reader
import java.lang.StringBuilder
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


interface ConfigState {

    val value: Any?

    val indices: IntRange
    val keys: Set<String>

    fun get(key: String): ConfigState
    fun get(index: Int): ConfigState
    fun add(value: Any)
    fun set(value: Any)
    fun merge(value: Any)

    fun path(p: String) = path(parseConfigPath(p))
    fun path(p: List<Any>): ConfigState {
        var state = this
        for (segment in p) {
            state = if (segment is Int) state.get(segment) else state.get(segment as String)
        }
        return state
    }

    val list: List<Any?>
    val plain: Any?

    val string: String? get() = plain?.toString()
    val int: Int? get() = plain?.let {
        (it as? Number)?.toInt() ?:
        (it as? Boolean)?.let { b -> if (b) 1 else 0 } ?:
        (it as? String)?.toIntOrNull()
    }

    val stringList: List<String> get() = list.mapNotNull { toConfigState(it, this::set).string }
    val stringSet: Set<String> get() = stringList.toSet()
    val flag: Boolean get() = plain?.let {
        it as? Boolean ?:
        (it as? Int)?.let { i -> i != 0 } ?:
        (it as? String)?.let { s -> s.toBoolean() }
    } ?: false
    val file: File? get() = string?.let { File(it) }

}

fun toConfigState(value: Any?, mutate: ConfigStateMutator): ConfigState {
    return mapValueByType(value,
        { map -> ConfigMutableMapState(map.asMutableMap(mutate), mutate) },
        { list -> ConfigMutableListState(list.asMutableList(mutate), mutate) },
        { plain -> ConfigPlainState(plain, mutate) }
    )
}

fun <T> List<T>.asMutableList(callback: (ArrayList<T>) -> Unit = {}): ArrayList<T> {
    return this as? ArrayList<T> ?: ArrayList(this).also(callback)
}

fun <K, V> Map<K, V>.asMutableMap(callback: (LinkedHashMap<K, V>) -> Unit = {}): LinkedHashMap<K, V> {
    return this as? LinkedHashMap<K, V> ?: LinkedHashMap(this).also(callback)
}

@Suppress("UNCHECKED_CAST")
fun <T> mapValueByType(
    value: Any?,
    mapHandler: (Map<String, Any>) -> T,
    listHandler: (List<Any?>) -> T,
    plainHandler: (Any?) -> T
): T  {
    return (value as? Map<String, Any>)?.let { mapHandler(it) } ?:
        (value as? List<Any?>)?.let { listHandler(it) } ?:
        plainHandler(value)
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

fun parseConfigPath(str: String): List<Any> {

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

fun parseConfigArgs(args: List<String>): ConfigMutableMapState {
    val optPrefix = "--"
    val defaultPath = parseConfigPath("args")
    val state = ConfigMutableMapState(mutableMapOf()) {}

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
            path = parseConfigPath(a.substring(optPrefix.length)).ifEmpty { defaultPath }
            continue
        }
        values.add(a)
    }
    flush()
    return state
}

fun loadConfigYaml(loader: (Yaml) -> Map<String, Any>) = ConfigMutableMapState(loader(Yaml()).asMutableMap()) {}
fun loadConfigYaml(stream: InputStream) = loadConfigYaml { yaml -> yaml.load(stream) }
fun loadConfigYaml(reader: Reader) = loadConfigYaml { yaml -> yaml.load(reader) }
fun loadConfigYaml(file: File) = FileReader(file).use { loadConfigYaml(it) }
fun loadConfigYaml(url: URL) = url.openStream().use { loadConfigYaml(it) }

package ru.vm.mpb.config

import java.io.File

@Suppress("UNCHECKED_CAST")
class ConfigValues(val map: Map<String, Any>) {
    fun getConfig(key: String) = ConfigValues(
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

}
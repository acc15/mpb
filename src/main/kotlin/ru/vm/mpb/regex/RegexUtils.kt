package ru.vm.mpb.regex

import java.lang.IllegalArgumentException

val ESCAPE_REGEX = Regex("(\\\\|\\\$(?!\\d+|\\{\\w+}))")
val GROUP_REGEX = Regex("(?<!\\\\)\\\$(?:(\\d+)|\\{(\\w+)})")
val UNESCAPE_REGEX = Regex("\\\\(.)")

fun escapeReplacement(replacement: String): String {
    return ESCAPE_REGEX.replace(replacement, "\\\\$1")
}

fun replaceByLookup(replacement: String, indexLookup: (Int) -> String, nameLookup: (String) -> String) =
    GROUP_REGEX.replace(replacement) { mr ->
        val name = mr.groupValues[2]
        if (name.isNotEmpty()) nameLookup(name) else indexLookup(mr.groupValues[1].toInt())
    }.replace(UNESCAPE_REGEX, "$1")

fun replaceByGroups(replacement: String, matchedGroups: Iterable<MatchGroupCollection>) = replaceByLookup(replacement,
    { index -> matchedGroups.firstNotNullOfOrNull { it[index]?.value }.orEmpty() },
    { name -> matchedGroups.firstNotNullOfOrNull { groupByNameOrNull(it, name)?.value }.orEmpty() }
)

fun groupByNameOrNull(groups: MatchGroupCollection, name: String): MatchGroup? = try {
    groups[name]
} catch (e: IllegalArgumentException) {
    null
}

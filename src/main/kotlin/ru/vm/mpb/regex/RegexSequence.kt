package ru.vm.mpb.regex

import kotlinx.serialization.Serializable

@Serializable
data class RegexSequence(
    val patterns: List<@Serializable(with=RegexAsStringSerializer::class) Regex>,
    val replacement: String
) {

    fun findAllMatches(lines: Sequence<String>, onMatch: (String) -> Unit) {
        val groups = mutableListOf<MatchGroupCollection>()
        for (line in lines) {
            findMatches(line, onMatch, groups)
        }
    }

    fun findMatches(
        chunk: String,
        onMatch: (String) -> Unit,
        groups: MutableList<MatchGroupCollection>,
    ) {
        val lastIndex = patterns.lastIndex

        var pos = 0
        while (pos < chunk.length) {

            val (i, m) = (0 .. minOf(lastIndex, groups.size)).firstNotNullOfOrNull {
                    i -> patterns[i].find(chunk, pos)?.let { m -> IndexedValue(i, m) }
            } ?: return

            groups.subList(i, groups.size).clear()
            groups.add(m.groups)
            pos = m.range.last + 1

            if (i == lastIndex) {
                onMatch(replaceByGroups(replacement, groups))
            }
        }
    }

}
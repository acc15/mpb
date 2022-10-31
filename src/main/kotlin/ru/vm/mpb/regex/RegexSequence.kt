package ru.vm.mpb.regex

data class RegexSequence(
    val patterns: List<Regex>,
    val replacement: String
) {

    fun findAllMatches(
        chunks: Iterable<String>,
        groups: MutableList<MatchGroupCollection> = mutableListOf(),
        matches: MutableList<String> = mutableListOf()
    ): List<String> {
        for (chunk in chunks) {
            findMatches(chunk, groups, matches)
        }
        return matches
    }

    fun findMatches(
        chunk: String,
        groups: MutableList<MatchGroupCollection>,
        matches: MutableList<String> = mutableListOf()
    ): List<String> {
        val lastIndex = patterns.lastIndex

        var pos = 0
        while (pos < chunk.length) {

            val (i, m) = (0 .. minOf(lastIndex, groups.size)).firstNotNullOfOrNull {
                    i -> patterns[i].find(chunk, pos)?.let { m -> IndexedValue(i, m) }
            } ?: return matches

            groups.subList(i, groups.size).clear()
            groups.add(m.groups)
            pos = m.range.last + 1

            if (i == lastIndex) {
                matches.add(replaceByGroups(replacement, groups))
            }
        }
        return matches
    }

}
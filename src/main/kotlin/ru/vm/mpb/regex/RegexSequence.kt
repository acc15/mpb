package ru.vm.mpb.regex

data class RegexSequence(
    val patterns: List<Regex>,
    val replacement: String
) {

    fun findAllMatches(chunks: List<String>): List<String> {
        val state = mutableListOf<MatchGroupCollection>()
        return chunks.flatMap { findMatches(state, it) }
    }

    fun findMatches(state: MutableList<MatchGroupCollection>, chunk: String): List<String> {
        var pos = 0
        val matches = mutableListOf<String>()
        while (pos < chunk.length) {

            for ((i, p) in patterns.withIndex()) {
                val isLast = i == patterns.lastIndex
                val m = p.find(chunk, pos)
                if (m != null) {
                    state.subList(i, state.size).clear()
                    state.add(m.groups)
                    pos = m.range.last + 1

                    if (isLast) {
                        // all patterns matched - compute replacement using state
                        matches.add(replaceByGroups(replacement, state))
                    }
                    break
                }
                if (isLast) {
                    return matches
                }
            }
        }
        return matches
    }

}
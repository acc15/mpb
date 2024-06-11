package ru.vm.mpb.regex

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ru.vm.mpb.config.BranchPattern

@Serializable
data class BranchPattern(

    /**
     * Pattern to match against user input
     */
    @Serializable(with = RegexAsStringSerializer::class)
    val input: Regex,

    /**
     * Dynamic branch pattern (which may use match groups from [input]) to find requested branch
     */
    val branch: String,

    /**
     * Index of branch to take.
     *
     * Negative values will take values from end of matched branch list, i.e. use `-1` for last element
     */
    val index: Int = -1

) {

    @Transient
    private val replacement = escapeReplacement(branch)

    companion object {

        fun getBranchMatch(patterns: List<Regex>, branch: String) = patterns
            .mapNotNull { it.matchEntire(branch) }
            .map { it.groupValues.getOrNull(1) ?: it.value }
            .firstOrNull()

        fun findMatch(patterns: List<BranchPattern>, branches: List<String>, text: String) = patterns
            .firstNotNullOfOrNull { it.findMatch(text, branches) }

    }

    fun getRegexes(text: String) = input.findAll(text)
        .map { Regex(input.replace(it.value, replacement)) }
        .toList()

    fun getMatchByIndex(matches: List<String>): String? = matches
        .getOrNull(if (index < 0) matches.size + index else index)

    fun findMatch(text: String, list: List<String>): String? {
        val patterns = getRegexes(text)
        if (patterns.isEmpty()) {
            return null
        }

        val matches = list.mapNotNull { getBranchMatch(patterns, it) }
        return getMatchByIndex(matches)
    }

}
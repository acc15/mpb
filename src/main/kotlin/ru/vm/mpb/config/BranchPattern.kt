package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config
import ru.vm.mpb.regex.escapeReplacement

data class BranchPattern(
    val input: Regex,
    val branch: String,
    val index: Int
) {

    val replacement = escapeReplacement(branch)

    companion object {
        fun fromConfig(cfg: Config) = BranchPattern(
            Regex(cfg.get("input").string.orEmpty()),
            cfg.get("branch").string.orEmpty(),
            parseIndex(cfg.get("index").string.orEmpty()) ?: 0
        )

        fun parseIndex(idx: String): Int? = when {
            idx.equals("first", true) -> 0
            idx.equals("last", true) -> -1
            else -> idx.toIntOrNull()
        }

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

    fun getMatchByIndex(matches: List<String>): String? = when {
        index < 0 && matches.isNotEmpty() -> matches.last()
        index >= 0 && index < matches.size -> matches[index]
        else -> null
    }

    fun findMatch(text: String, list: List<String>): String? {
        val patterns = getRegexes(text)
        if (patterns.isEmpty()) {
            return null
        }

        val matches = list.mapNotNull { getBranchMatch(patterns, it) }
        return getMatchByIndex(matches)
    }

}
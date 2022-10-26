package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config

data class BranchPattern(
    val input: Regex,
    val branch: String,
    val index: Int
) {

    companion object {
        val REPLACE_ESCAPE_REGEX = Regex("(\\\\|\\\$(?!\\d|\\{.+}))")
        fun escapeReplacement(replacement: String): String {
            return REPLACE_ESCAPE_REGEX.replace(replacement, "\\\\$1")
        }

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
    }

    fun findBranch(input: String, list: List<String>): String? {
        val escapedBranch = escapeReplacement(branch)
        val replacedBranch = this.input.replace(input, escapedBranch)
        val branchRegex = Regex(replacedBranch)
        val matches = list
            .mapNotNull { branchRegex.matchEntire(it) }
            .map { it.groupValues.getOrNull(1) ?: it.value }

        return when {
            index < 0 && matches.isNotEmpty() -> return matches.last()
            index >= 0 && index < matches.size -> return matches[index]
            else -> null
        }
    }
}
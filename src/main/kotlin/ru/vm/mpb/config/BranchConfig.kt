package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config

data class BranchConfig(
    val default: String?,
    val patterns: List<BranchPattern>,
    val noFetch: Boolean,
    val noPull: Boolean
) {
    companion object {
        fun fromConfig(cfg: Config) = BranchConfig(
            cfg.get("default").string,
            cfg.get("patterns").configList.map(BranchPattern::fromConfig),
            cfg.shorthand.get("noFetch").flag,
            cfg.shorthand.get("noPull").flag
        )
    }
}
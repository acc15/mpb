package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config

data class BranchConfig(
    val default: String?,
    val patterns: List<BranchPattern>
) {
    companion object {
        fun fromConfig(cfg: Config) = BranchConfig(
            cfg.get("default").string,
            cfg.get("patterns").configList.map(BranchPattern::fromConfig)
        )
    }
}
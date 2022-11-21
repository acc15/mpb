package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config
import java.nio.file.Path
import kotlin.io.path.Path

data class GitConfig(
    val default: String?,
    val patterns: List<BranchPattern>,
    val noFetch: Boolean,
    val noRebase: Boolean,
    val ignore: Set<Path>
) {
    companion object {
        fun fromConfig(cfg: Config) = GitConfig(
            cfg.get("default").string,
            cfg.get("patterns").configList.map(BranchPattern::fromConfig),
            cfg.shorthand.get("noFetch").flag,
            cfg.shorthand.get("noRebase").flag,
            cfg.shorthand.get("ignore").stringList.mapTo(LinkedHashSet(), ::Path)
        )
    }
}
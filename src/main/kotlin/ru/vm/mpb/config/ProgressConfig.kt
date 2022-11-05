package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config
import ru.vm.mpb.regex.RegexSequence

data class ProgressConfig(
    val cmd: List<String>,
    val plan: RegexSequence,
    val build: RegexSequence
) {

    companion object {

        fun regexSequence(cfg: Config) = RegexSequence(
            cfg.get("patterns").stringList.map { Regex(it) },
            cfg.get("replacement").string ?: ""
        )

        fun fromConfig(cfg: Config) = ProgressConfig(
            cfg.get("cmd").stringList,
            regexSequence(cfg.get("plan")),
            regexSequence(cfg.get("build"))
        )
    }

}
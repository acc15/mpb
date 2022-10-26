package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config

data class JiraConfig(
    val url: String,
    val project: String
) {
    companion object {
        fun fromConfig(cfg: Config) = JiraConfig(
            cfg.get("url").string.orEmpty(),
            cfg.get("project").string.orEmpty()
        )
    }
}
package ru.vm.mpb.config

import ru.vm.mpb.config.state.Config

data class ArgConfig(
    val project: Map<String, List<String>>,
    val active: Map<String, List<String>>,
    val common: List<String>,
    val command: String
) {

    companion object {
        fun fromConfig(cfg: Config, projects: Set<String>, filters: IncludeExclude<String>): ArgConfig {
            val argMap = cfg.get("args").configMap.mapValues { it.value.stringList }
            val project = argMap.filter { it.key != "" }
            val defaultArgs = argMap[""] ?: emptyList()
            val command = defaultArgs.firstOrNull().orEmpty()
            val common = defaultArgs.drop(1)
            val active = projects.filter(filters::includes).associateWith { project[it] ?: common }
            return ArgConfig(project, active, common, command)
        }
    }

}
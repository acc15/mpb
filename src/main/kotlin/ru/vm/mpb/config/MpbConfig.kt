package ru.vm.mpb.config

import java.io.File

const val DEFAULT_KEY = "default"

class MpbConfig {
    var debug: Boolean = false
    var defaultBranch: String = "master"
    var projects: Map<String, ProjectConfig> = emptyMap()
    var jira = JiraConfig()
    var ticket = TicketConfig()
    var build: Map<String, BuildConfig> = emptyMap()

    fun getDefaultBranch(proj: String) = projects[proj]?.defaultBranch ?: defaultBranch
}

class BuildConfig {
    var profiles = emptyMap<String, List<String>>()
    var env = emptyMap<String, String>()
}

class JiraConfig {
    var url = ""
    var project = ""
}

class TicketConfig {
    var dir = File("tickets")
}

class ProjectConfig {
    var dir = File("").absoluteFile!!
    var deps = emptySet<String>()
    var build = DEFAULT_KEY
    var defaultBranch: String? = null
}
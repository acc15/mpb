package ru.vm.mpb.util

import ru.vm.mpb.config.MpbConfig

val TICKET_REGEX = Regex("(?:(?<url>.*)/)?(?:(?<proj>.*)-)?(?<num>.*)")

data class JiraTicket(
    val url: String,
    val project: String,
    val number: String
) {
    val id: String get() = "${project}-${number}"
    val fullUrl: String get() = "${url}/$id"
}

fun parseJiraTicket(cfg: MpbConfig, str: String): JiraTicket? = TICKET_REGEX.matchEntire(str)?.run {
    JiraTicket(
        groups["url"]?.value ?: cfg.jira.url,
        groups["proj"]?.value ?: cfg.jira.project,
        groups["num"]?.value ?: ""
    )
}

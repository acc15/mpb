package ru.vm.mpb.util

import ru.vm.mpb.config.MpbConfig

data class JiraTicket(
    val url: String,
    val project: String,
    val number: String
) {
    val id: String get() = "${project}-${number}"
    val fullUrl: String get() = "${url}/${id}"

    companion object {
        val TICKET_REGEX = Regex("(?:(?<url>.*)/)?(?:(?<proj>.*)-)?(?<num>.*)")

        @JvmStatic
        fun parse(cfg: MpbConfig, str: String): JiraTicket? = TICKET_REGEX.matchEntire(str)?.run {
            JiraTicket(
                groups["url"]?.value ?: cfg.jira.url,
                groups["proj"]?.value ?: cfg.jira.project,
                groups["num"]?.value ?: ""
            )
        }
    }
}

package ru.vm.mpb.cmd

import ru.vm.mpb.config.MpbConfig
import ru.vm.mpb.util.parseJiraTicket
import java.awt.Desktop
import java.net.URI

object JiraCmd: Cmd(
    setOf("j", "jira"),
    "opens jira issue in browser",
    "<issueid or url...>"
) {
    override fun execute(cfg: MpbConfig, args: List<String>) {
        if (!Desktop.isDesktopSupported()) {
            println("AWT desktop not supported. Unable to open URLs")
            return
        }

        val d = Desktop.getDesktop()
        for (arg in args) {
            val t = parseJiraTicket(cfg, arg)
            if (t != null) {
                val fullUri = URI.create(t.fullUrl)
                d.browse(fullUri)
            }
        }
    }

}
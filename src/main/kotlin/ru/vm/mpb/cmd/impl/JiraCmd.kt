package ru.vm.mpb.cmd.impl

import ru.vm.mpb.cmd.Cmd
import ru.vm.mpb.cmd.CmdDesc
import ru.vm.mpb.cmd.ctx.CmdContext
import ru.vm.mpb.config.MpbConfig
import ru.vm.mpb.util.JiraTicket
import java.awt.Desktop
import java.net.URI

private val DESC = CmdDesc(
    setOf("j", "jira"),
    "opens jira issue in browser",
    "<issueid or url...>"
)

object  JiraCmd: Cmd(DESC) {
    override fun execute(ctx: CmdContext) {
        if (!Desktop.isDesktopSupported()) {
            println("AWT desktop not supported. Unable to open URLs")
            return
        }

        val d = Desktop.getDesktop()
        for (arg in ctx.args) {
            val t = JiraTicket.parse(ctx.cfg, arg)
            if (t != null) {
                val fullUri = URI.create(t.fullUrl)
                d.browse(fullUri)
            }
        }
    }

}
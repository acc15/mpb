package ru.vm.mpb.cmd.impl

import ru.vm.mpb.cmd.Cmd
import ru.vm.mpb.cmd.CmdDesc
import ru.vm.mpb.cmd.ctx.CmdContext
import ru.vm.mpb.util.JiraTicket

private val DESC = CmdDesc(
    setOf("j", "jira"),
    "opens jira issue in browser",
    "<issueid or url...>"
)

fun openUrlCmd(url: String) = System.getProperty("os.name")!!.let {
    when {
        it.startsWith("Windows") -> listOf("explorer", url)
        it.startsWith("Mac") -> listOf("open", url)
        else -> listOf("xdg-open", url)
    }
}

object  JiraCmd: Cmd(DESC) {
    override fun execute(ctx: CmdContext) {
        for (arg in ctx.args) {
            val t = JiraTicket.parse(ctx.cfg, arg)
            if (t != null) {
                ctx.exec(openUrlCmd(t.fullUrl)).start()
            }
        }
    }
}
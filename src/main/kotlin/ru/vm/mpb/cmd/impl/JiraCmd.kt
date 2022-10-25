package ru.vm.mpb.cmd.impl

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

object JiraCmd: Cmd(DESC) {
    override suspend fun execute(ctx: CmdContext): Boolean = coroutineScope {
        ctx.args.mapNotNull { JiraTicket.parse(ctx.cfg, it) }.map {
            async { ctx.exec(openUrlCmd(it.fullUrl)).success() }
        }.awaitAll().all { it }
    }
}
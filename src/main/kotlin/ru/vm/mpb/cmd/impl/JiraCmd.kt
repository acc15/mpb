package ru.vm.mpb.cmd.impl

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import ru.vm.mpb.cmd.Cmd
import ru.vm.mpb.cmd.CmdDesc
import ru.vm.mpb.cmd.ctx.CmdContext
import ru.vm.mpb.util.JiraTicket
import ru.vm.mpb.util.success

object JiraCmd: Cmd {

    override val desc = CmdDesc(
        listOf("j", "jira"),
        "opens jira issue in browser",
        "<issueid or url...>"
    )

    override suspend fun execute(ctx: CmdContext): Boolean = coroutineScope {
        ctx.args.mapNotNull { JiraTicket.parse(ctx.cfg, it) }.map {
            async { ctx.exec(openUrlCmd(it.fullUrl)).success() }
        }.awaitAll().all { it }
    }

    private fun openUrlCmd(url: String) = with(System.getProperty("os.name").orEmpty()) {
        when {
            startsWith("Windows") -> listOf("explorer", url)
            startsWith("Mac") -> listOf("open", url)
            else -> listOf("xdg-open", url)
        }
    }

}
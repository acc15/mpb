package ru.vm.mpb.cmd.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.vm.mpb.cmd.CmdDesc
import ru.vm.mpb.cmd.ParallelCmd
import ru.vm.mpb.cmd.ctx.ProjectContext

private val DESC = CmdDesc(
    setOf("p", "pull"),
    "pull all repos",
    ""
)

object PullCmd: ParallelCmd(DESC) {

    override suspend fun parallelExecute(ctx: ProjectContext) {
        withContext(Dispatchers.IO) {
            ctx.print("pulling...")

            val success = ctx.exec("git", "pull", "--rebase").success()
            ctx.print(if (success) "done" else "error")
        }
    }

}
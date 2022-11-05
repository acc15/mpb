package ru.vm.mpb.cmd.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.vm.mpb.cmd.CmdDesc
import ru.vm.mpb.cmd.ParallelCmd
import ru.vm.mpb.cmd.ctx.ProjectContext
import ru.vm.mpb.printer.PrintStatus
import ru.vm.mpb.util.success

object PullCmd: ParallelCmd {

    override val desc = CmdDesc(
        listOf("p", "pull"),
        "pull all repos",
        ""
    )

    override suspend fun parallelExecute(ctx: ProjectContext): Boolean = withContext(Dispatchers.IO) {
        ctx.print("pulling...")

        val success = ctx.exec("git", "pull", "--rebase").success()
        if (success) {
            ctx.print("done", PrintStatus.SUCCESS)
        } else {
            ctx.print("error", PrintStatus.ERROR)
        }
        success
    }

}
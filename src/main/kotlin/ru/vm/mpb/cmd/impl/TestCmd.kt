package ru.vm.mpb.cmd.impl

import kotlinx.coroutines.delay
import ru.vm.mpb.cmd.CmdDesc
import ru.vm.mpb.cmd.ParallelCmd
import ru.vm.mpb.cmd.ctx.ProjectContext
import ru.vm.mpb.progressbar.IndeterminateProgressBar

object TestCmd: ParallelCmd {

    override val desc = CmdDesc(listOf("x"), "test", "")

    override suspend fun parallelExecute(ctx: ProjectContext): Boolean {
        val p = IndeterminateProgressBar(50)
        for (i in 0..255) {
            ctx.print(ctx.ansi.apply(p.update()))
            delay(50)
        }
        return true
    }
}
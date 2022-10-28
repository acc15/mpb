package ru.vm.mpb.cmd.impl

import kotlinx.coroutines.delay
import ru.vm.mpb.cmd.CmdDesc
import ru.vm.mpb.cmd.ParallelCmd
import ru.vm.mpb.cmd.ctx.ProjectContext
import ru.vm.mpb.progress.IndeterminateProgressBar
import kotlin.random.Random

object TestCmd: ParallelCmd {

    override val desc = CmdDesc(listOf("x"), "test", "")

    override suspend fun parallelExecute(ctx: ProjectContext): Boolean {
        val w = 20
        val p = IndeterminateProgressBar(Random.nextInt(IndeterminateProgressBar.maxPosition(w)))
        for (i in 0..100) {
            val a = ctx.ansi.a("building cmd ")
            p.update(w, a)
            ctx.print(a.toString())
            delay(100)
        }
        return true
    }
}
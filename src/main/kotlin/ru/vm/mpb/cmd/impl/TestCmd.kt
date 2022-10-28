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
        delay(Random.nextInt(2000).toLong())
        for (i in 0..100) {
            ctx.print(listOf("Multi ${Random.nextInt()}", "line ${Random.nextDouble()}", "for project ${ctx.key}").joinToString(System.lineSeparator()))
            delay(100)
        }
        return true
    }
}
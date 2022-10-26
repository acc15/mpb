package ru.vm.mpb.cmd.impl

import kotlinx.coroutines.delay
import ru.vm.mpb.cmd.CmdDesc
import ru.vm.mpb.cmd.ParallelCmd
import ru.vm.mpb.cmd.ctx.ProjectContext

object TestCmd: ParallelCmd {

    override val desc = CmdDesc(listOf("x"), "test", "")

    override suspend fun parallelExecute(ctx: ProjectContext): Boolean {
        for (i in 0..100) {
            ctx.print(if (i % 2 == 0) "loooooooooooooong liiiiiiiiiiiiine: $i" else "short line")
            delay(500)
        }
        return true
    }
}
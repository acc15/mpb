package ru.vm.mpb.cmd

import kotlinx.coroutines.*
import ru.vm.mpb.cmd.ctx.CmdContext
import ru.vm.mpb.cmd.ctx.ProjectContext
import ru.vm.mpb.config.MpbConfig

abstract class ParallelCmd(desc: CmdDesc): Cmd(desc) {
    override suspend fun execute(ctx: CmdContext): Boolean = coroutineScope {
        ctx.cfg.activeArgs.keys
            .map {
                async {
                    parallelExecute(ctx.projectContext(it))
                }
            }
            .awaitAll()
            .all { it }
    }

    abstract suspend fun parallelExecute(ctx: ProjectContext): Boolean
}
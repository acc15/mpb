package ru.vm.mpb.cmd

import kotlinx.coroutines.*
import ru.vm.mpb.cmd.ctx.CmdContext
import ru.vm.mpb.cmd.ctx.ProjectContext
import ru.vm.mpb.config.MpbConfig

interface ParallelCmd : Cmd {
    override suspend fun execute(ctx: CmdContext): Boolean = coroutineScope {
        ctx.cfg.args.active.keys.map {
            async {
                parallelExecute(ctx.projectContext(it))
            }
        }.awaitAll().all { it }
    }

    suspend fun parallelExecute(ctx: ProjectContext): Boolean
}
package ru.vm.mpb.cmd

import kotlinx.coroutines.*
import ru.vm.mpb.cmd.ctx.CmdContext
import ru.vm.mpb.cmd.ctx.ProjectContext
import ru.vm.mpb.printer.PrintStatus

interface ProjectCmd : Cmd {
    override suspend fun execute(ctx: CmdContext): Boolean{
        val keys = ctx.cfg.args.active.keys
        if (keys.isEmpty()) {
            ctx.print("no one project is active", PrintStatus.ERROR)
            return false
        }
        return coroutineScope {
            keys.map {
                async {
                    parallelExecute(ctx.projectContext(it))
                }
            }.awaitAll().all { it }
        }
    }

    suspend fun parallelExecute(ctx: ProjectContext): Boolean
}
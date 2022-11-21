package ru.vm.mpb.cmd

import kotlinx.coroutines.*
import ru.vm.mpb.cmd.ctx.CmdContext
import ru.vm.mpb.cmd.ctx.ProjectContext
import ru.vm.mpb.printer.PrintStatus

interface ProjectCmd : Cmd {
    override suspend fun execute(ctx: CmdContext): Boolean {
        val keys = ctx.cfg.args.active.keys
        if (keys.isEmpty()) {
            ctx.print("no one project is active", PrintStatus.ERROR)
            return false
        }
        return if (ctx.cfg.seq) runSequentially(keys, ctx) else runParallel(keys, ctx)
    }

    suspend fun runSequentially(keys: Set<String>, ctx: CmdContext) =
        keys.map { projectExecute(ctx.projectContext(it)) }.all { it }

    suspend fun runParallel(keys: Set<String>, ctx: CmdContext) = coroutineScope {
        keys.map { async { projectExecute(ctx.projectContext(it)) } }.awaitAll().all { it }
    }

    suspend fun projectExecute(ctx: ProjectContext): Boolean
}
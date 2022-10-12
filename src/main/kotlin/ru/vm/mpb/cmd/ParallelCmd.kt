package ru.vm.mpb.cmd

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.vm.mpb.cmd.ctx.CmdContext
import ru.vm.mpb.cmd.ctx.ProjectContext
import ru.vm.mpb.config.MpbConfig

abstract class ParallelCmd(desc: CmdDesc): Cmd(desc) {
    override fun execute(ctx: CmdContext) {
        runBlocking(Dispatchers.Default) {
            for (p in ctx.cfg.activeArgs.keys) {
                launch {
                    parallelExecute(ctx.projectContext(p))
                }
            }
        }
    }

    abstract suspend fun parallelExecute(ctx: ProjectContext)
}
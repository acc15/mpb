package ru.vm.mpb.cmd

import ru.vm.mpb.cmd.ctx.CmdContext

interface Cmd {
    val desc: CmdDesc
    suspend fun execute(ctx: CmdContext): Boolean
}




package ru.vm.mpb.cmd

import ru.vm.mpb.cmd.ctx.CmdContext
import ru.vm.mpb.config.MpbConfig

abstract class Cmd(val desc: CmdDesc) {

    abstract suspend fun execute(ctx: CmdContext): Boolean

    fun printUsage(cfg: MpbConfig) {
        println(desc.usage(cfg))
    }

}




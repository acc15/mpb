package ru.vm.mpb.cmd

import ru.vm.mpb.cmd.ctx.CmdContext

abstract class Cmd(val desc: CmdDesc) {

    abstract suspend fun execute(ctx: CmdContext): Boolean

    fun printUsage() {
        println(desc.usage)
    }

}




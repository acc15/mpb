package ru.vm.mpb.cmd

import ru.vm.mpb.cmd.ctx.CmdContext
import kotlin.system.exitProcess

abstract class Cmd(val desc: CmdDesc) {

    abstract suspend fun execute(ctx: CmdContext): Boolean

    fun printUsageAndExit(): Boolean {
        println(desc.usage)
        return false
    }

}




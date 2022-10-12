package ru.vm.mpb.cmd

import ru.vm.mpb.cmd.ctx.CmdContext
import kotlin.system.exitProcess

abstract class Cmd(val desc: CmdDesc) {

    abstract fun execute(ctx: CmdContext)

    fun printUsageAndExit(): Nothing {
        println(desc.usage)
        exitProcess(1)
    }

}




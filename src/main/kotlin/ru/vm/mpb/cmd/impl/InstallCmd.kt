package ru.vm.mpb.cmd.impl

import ru.vm.mpb.cmd.Cmd
import ru.vm.mpb.cmd.CmdDesc
import ru.vm.mpb.cmd.ctx.CmdContext

object InstallCmd: Cmd {

    override val desc = CmdDesc(
        listOf("i", "install"),
        "creates mpb config and script",
        "<name>"
    )

    override suspend fun execute(ctx: CmdContext): Boolean {
        TODO("Not yet implemented")
    }
}
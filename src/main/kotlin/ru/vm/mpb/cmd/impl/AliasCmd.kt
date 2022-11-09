package ru.vm.mpb.cmd.impl

import org.fusesource.jansi.AnsiConsole
import ru.vm.mpb.cmd.Cmd
import ru.vm.mpb.cmd.CmdDesc
import ru.vm.mpb.cmd.ctx.CmdContext
import ru.vm.mpb.config.MpbEnv

object AliasCmd: Cmd {

    override val desc = CmdDesc(
        listOf("a", "alias"),
        "Prints bash aliases/doskey/PowerShell aliases for every profile",
        ""
    )

    override suspend fun execute(ctx: CmdContext): Boolean {
        for (p in ctx.cfg.profiles) {
            AnsiConsole.out().println("alias $p=\"source ${MpbEnv.home}/bin/mpb_profile $p\"")
        }
        return true
    }
}
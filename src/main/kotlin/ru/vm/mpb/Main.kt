package ru.vm.mpb

import com.github.ajalt.clikt.completion.completionOption
import com.github.ajalt.clikt.core.subcommands
import ru.vm.mpb.commands.BuildCommand
import ru.vm.mpb.commands.MpbCommand
import ru.vm.mpb.commands.PullCommand
import ru.vm.mpb.commands.SwitchCommand

fun main(args: Array<String>) = MpbCommand()
    .completionOption()
    .subcommands(
        BuildCommand(),
        SwitchCommand(),
        PullCommand()
    )
    .main(args)

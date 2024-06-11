package ru.vm.mpb.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional

class SwitchCommand : CliktCommand() {

    val branch: String? by argument(help = "branch name or one of branch pattern match").optional()

    override fun run() {
        println("switch")
    }
}
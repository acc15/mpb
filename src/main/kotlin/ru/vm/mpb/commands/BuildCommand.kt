package ru.vm.mpb.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default

class BuildCommand : CliktCommand() {

    val command: String by argument().default("default")

    override fun run() {
        println("build")
    }
}
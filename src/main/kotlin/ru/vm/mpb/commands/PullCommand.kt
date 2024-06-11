package ru.vm.mpb.commands

import com.github.ajalt.clikt.core.CliktCommand

class PullCommand : CliktCommand() {
    override fun run() {
        println("pull")
    }
}
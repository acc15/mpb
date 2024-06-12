package ru.vm.mpb.commands

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int

class OutputOptions : OptionGroup(name = "Output Options") {
    val plain: Boolean by option(
        "-p", "--plain",
        help = "Outputs all messages, by default only recent messages are displayed (Status mode)"
    ).flag()

    val monochrome: Boolean by option("-m", "--monochrome", help = "Disables color output").flag()
    val width: Int by option("-w", "--width", help = "Default terminal width").int().default(80)
}

package ru.vm.mpb.commands

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.options.transformAll

class ProjectOptions : OptionGroup(name = "Project Options") {
    /** Include set */
    val include: Set<String> by option("-I", "--include", help = "Includes specified project")
        .split(",")
        .transformAll { it.flatten().toSet() }

    /** Exclude set */
    val exclude: Set<String> by option("-E", "--exclude", help = "Excludes specified project")
        .split(",")
        .transformAll { it.flatten().toSet() }
}

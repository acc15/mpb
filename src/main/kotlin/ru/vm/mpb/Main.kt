package ru.vm.mpb

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import ru.vm.mpb.cmd.BuildCmd
import ru.vm.mpb.cmd.CheckoutCmd
import ru.vm.mpb.cmd.JiraCmd
import ru.vm.mpb.cmd.TicketCmd
import ru.vm.mpb.config.MpbConfig
import java.io.File
import kotlin.system.exitProcess

const val PROGRAM_NAME = "mpb"

val ALL_CMDS = listOf(
    JiraCmd,
    TicketCmd,
    CheckoutCmd,
    BuildCmd
)

val ALL_CMDS_MAP = ALL_CMDS.flatMap { c -> c.names.map { it to c } }.toMap()

fun printHelp(msg: String = "") {

    if (msg.isNotEmpty()) {
        println(msg)
        println()
    }

    println("Usage: $PROGRAM_NAME <command> [arguments]")
    println()
    println("Supported commands: ")
    println()

    for (cmd in ALL_CMDS) {
        println(cmd.help)
    }

}

fun main(args: Array<String>) {

    val cfg = ObjectMapper(YAMLFactory()).registerKotlinModule().readValue<MpbConfig>(File("mpb.yaml"))

    if (args.isEmpty()) {
        printHelp()
        exitProcess(1)
    }

    val cmdName = args[0]
    val cmd = ALL_CMDS_MAP[cmdName]
    if (cmd == null) {
        printHelp("Unknown command: $cmdName")
        exitProcess(1)
    }

    val cmdArgs = args.toList().subList(1, args.size)
    cmd.execute(cfg, cmdArgs)

}


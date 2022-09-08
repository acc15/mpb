package ru.vm.mpb

import org.yaml.snakeyaml.Yaml
import ru.vm.mpb.cmd.*
import ru.vm.mpb.config.MpbConfig
import java.io.FileReader
import kotlin.system.exitProcess

const val PROGRAM_NAME = "mpb"

val ALL_CMDS = listOf(
    JiraCmd,
    TicketCmd,
    CheckoutCmd,
    BuildCmd,
    PullCmd
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

    val cfgPath = System.getenv("MPB_CONFIG") ?: "mpb.yaml"
    val cfg = FileReader(cfgPath).use { Yaml().loadAs(it, MpbConfig::class.java) }
    if (System.getenv("MPB_DEBUG")?.let { it.toBoolean() } == true) {
        cfg.debug = true
    }

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


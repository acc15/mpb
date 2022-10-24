package ru.vm.mpb

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import ru.vm.mpb.cmd.ctx.CmdContext
import ru.vm.mpb.cmd.impl.*
import ru.vm.mpb.config.MpbConfig
import kotlin.system.exitProcess

val ALL_CMDS = listOf(
    JiraCmd,
    TicketCmd,
    CheckoutCmd,
    BuildCmd,
    PullCmd
)

val ALL_CMDS_MAP = ALL_CMDS.flatMap { c -> c.desc.names.map { it to c } }.toMap()

fun printHelp(cfg: MpbConfig, msg: String = "") {
    if (msg.isNotEmpty()) {
        println(msg)
        println()
    }

    println("Usage: ${cfg.name} <command> [arguments]")
    println()
    println("Supported commands: ")
    println()

    for (cmd in ALL_CMDS) {
        println(cmd.desc.help(cfg))
    }
}

fun main(args: Array<String>) {
    val success = runBlocking(Dispatchers.Default) {
        val cfg = MpbConfig.parse(args)
        if (cfg.command.isEmpty()) {
            printHelp(cfg)
            return@runBlocking false
        }

        val cmd = ALL_CMDS_MAP[cfg.command]
        if (cmd == null) {
            printHelp(cfg, "Unknown command: ${cfg.command}")
            return@runBlocking false
        }
        cmd.execute(CmdContext(cfg))
    }
    exitProcess(if (success) 0 else 1)
}


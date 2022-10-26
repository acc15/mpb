package ru.vm.mpb

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import ru.vm.mpb.cmd.ctx.CmdContext
import ru.vm.mpb.cmd.impl.*
import ru.vm.mpb.config.MpbConfig
import ru.vm.mpb.printer.createPrinter
import ru.vm.mpb.util.withJansi
import kotlin.system.exitProcess

val ALL_CMDS = listOf(
    JiraCmd,
    TicketCmd,
    CheckoutCmd,
    BuildCmd,
    PullCmd,
    TestCmd
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
    val success = withJansi {
        runBlocking(Dispatchers.Default) {

            val cfg = MpbConfig.parse(args)
            if (cfg.args.command.isEmpty()) {
                printHelp(cfg)
                return@runBlocking false
            }

            val cmd = ALL_CMDS_MAP[cfg.args.command]
            if (cmd == null) {
                printHelp(cfg, "Unknown command: ${cfg.args.command}")
                return@runBlocking false
            }

            createPrinter(cfg).use {
                cmd.execute(CmdContext(cfg, it))
            }
        }
    }
    exitProcess(if (success) 0 else 1)
}


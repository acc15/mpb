package ru.vm.mpb

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.fusesource.jansi.Ansi
import ru.vm.mpb.cmd.ctx.CmdContext
import ru.vm.mpb.cmd.impl.*
import ru.vm.mpb.config.MpbConfig
import ru.vm.mpb.printer.createPrinter
import ru.vm.mpb.jansi.withJansi
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

    println(Ansi.ansi().bold().a("Usage: ").boldOff().a(cfg.name).a(" <command> [arguments]"))
    println()
    println(Ansi.ansi().bold().a("Supported commands: ").reset())
    println()

    for (cmd in ALL_CMDS) {
        println(cmd.desc.help(cfg))
    }
}

fun main(args: Array<String>) {
    val cfg = MpbConfig.parse(args)
    val success = withJansi(cfg.output.noAnsi) {
        runBlocking(Dispatchers.Default) {
            runProgram(this, cfg)
        }
    }
    exitProcess(if (success) 0 else 1)
}

suspend fun runProgram(scope: CoroutineScope, cfg: MpbConfig): Boolean {
    if (cfg.args.command.isEmpty()) {
        printHelp(cfg)
        return false
    }

    val cmd = ALL_CMDS_MAP[cfg.args.command]
    if (cmd == null) {
        printHelp(cfg, "Unknown command: ${cfg.args.command}")
        return false
    }

    return scope.createPrinter(cfg).use {
        cmd.execute(CmdContext(cfg, it))
    }
}

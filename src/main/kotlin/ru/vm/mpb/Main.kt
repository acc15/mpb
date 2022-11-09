package ru.vm.mpb

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.fusesource.jansi.AnsiConsole
import org.fusesource.jansi.AnsiPrintStream
import ru.vm.mpb.cmd.ctx.CmdContext
import ru.vm.mpb.cmd.impl.*
import ru.vm.mpb.config.MpbConfig
import ru.vm.mpb.ansi.applyIf
import ru.vm.mpb.ansi.join
import ru.vm.mpb.printer.createPrinter
import ru.vm.mpb.ansi.withAnsi
import kotlin.system.exitProcess

val ALL_CMDS = listOf(
    JiraCmd,
    TicketCmd,
    CheckoutCmd,
    BuildCmd,
    PullCmd,
    AliasCmd,
    TestCmd
)

val ALL_CMDS_MAP = ALL_CMDS.flatMap { c -> c.desc.names.map { it to c } }.toMap()

fun printHelp(out: AnsiPrintStream, cfg: MpbConfig, msg: String = "") {
    out.println(cfg.output.ansi.get()
        .applyIf(msg.isNotEmpty()) { it.a(msg).newline().newline() }
        .bold().a("Usage: ").boldOff().a(cfg.name).a(" <command> [arguments]").newline().newline()
        .bold().a("Supported commands: ").reset().newline().newline()
        .join(ALL_CMDS, System.lineSeparator()) { a, it -> a.apply(it.desc.help(cfg.name)) })
}

fun main(args: Array<String>) {
    val success = withAnsi {
        val cfg = MpbConfig.parse(*args)
        runBlocking(Dispatchers.Default) {
            runProgram(this, cfg)
        }
    }
    exitProcess(if (success) 0 else 1)
}

suspend fun runProgram(scope: CoroutineScope, cfg: MpbConfig): Boolean {
    val out = AnsiConsole.out()
    if (cfg.args.command.isEmpty()) {
        printHelp(out, cfg)
        return false
    }

    val cmd = ALL_CMDS_MAP[cfg.args.command]
    if (cmd == null) {
        printHelp(out, cfg, "Unknown command: ${cfg.args.command}")
        return false
    }

    return scope.createPrinter(cfg, out).use {
        cmd.execute(CmdContext(cfg, it))
    }
}

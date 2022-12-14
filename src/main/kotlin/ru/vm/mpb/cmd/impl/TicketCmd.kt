package ru.vm.mpb.cmd.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.vm.mpb.cmd.Cmd
import ru.vm.mpb.cmd.CmdDesc
import ru.vm.mpb.cmd.ctx.CmdContext
import ru.vm.mpb.config.MpbEnv
import ru.vm.mpb.printer.PrintStatus
import ru.vm.mpb.util.JiraTicket
import ru.vm.mpb.io.deepMove
import java.nio.file.Files
import java.util.stream.Collectors
import kotlin.io.path.*

object TicketCmd: Cmd {

    override val desc = CmdDesc(
        listOf("t", "ticket"),
        "make ticket dir",
        "<ticket id> [-o | --overwrite (overwrite directory using new description)] [description...]"
    )

    override suspend fun execute(ctx: CmdContext): Boolean {

        val t = ctx.args.firstOrNull()?.let { JiraTicket.parse(ctx.cfg, it) }
        if (t == null) {
            ctx.print(ctx.ansi.apply(desc.usage(ctx.cfg.name)), PrintStatus.ERROR)
            return false
        }

        val overwrite = ctx.cfg.ticket.overwrite

        val desc = ctx.args.drop(1).joinToString(" ").replace(' ', '_').ifBlank { null }

        val ticketDir = ctx.cfg.ticket.dir
        val suggestedDir = ticketDir.resolve(desc?.let { "${t.id}_${it}" } ?: t.id)

        ctx.print("looking for same ticket dirs")
        val ticketDirs = withContext(Dispatchers.IO) {
            ticketDir.listDirectoryEntries()
                .filter { it.isDirectory() }
                .filter { it.name.startsWith(t.id) }
                .toMutableSet()
        }

        val targetDir = if (overwrite || ticketDirs.isEmpty()) suggestedDir else ticketDirs.maxBy { it.name.length }

        ticketDirs.remove(targetDir)
        if (!targetDir.isDirectory()) {
            ctx.print("creating $targetDir")
            targetDir.deleteIfExists()
            targetDir.createDirectory()
        }

        for (d in ticketDirs) {
            ctx.print("moving $d to $targetDir")
            deepMove(d, targetDir)
        }

        ctx.print("done: $targetDir", PrintStatus.SUCCESS)
        MpbEnv.cd.writeText(targetDir.toString())

        return true

    }
}


package ru.vm.mpb.cmd.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.vm.mpb.cmd.Cmd
import ru.vm.mpb.cmd.CmdDesc
import ru.vm.mpb.cmd.ctx.CmdContext
import ru.vm.mpb.printer.PrintStatus
import ru.vm.mpb.util.JiraTicket
import ru.vm.mpb.util.deepMove
import java.nio.file.Files
import java.util.stream.Collectors
import kotlin.io.path.*

private val DESC = CmdDesc(
    setOf("t", "ticket"),
    "make ticket dir",
    "<ticket id> [-o | --overwrite (overwrite directory using new description)] [description...]"
)

object TicketCmd: Cmd(DESC) {

    override suspend fun execute(ctx: CmdContext): Boolean {

        val t = ctx.args.firstOrNull()?.let { JiraTicket.parse(ctx.cfg, it) }
        if (t == null) {
            printUsage(ctx.cfg)
            return false
        }

        val overwrite = ctx.cfg.ticket.overwrite

        val desc = ctx.args.drop(1).joinToString(" ").replace(' ', '_').ifBlank { null }

        val ticketDir = ctx.cfg.ticket.dir.toPath()
        val suggestedDir = ticketDir.resolve(desc?.let { "${t.id}_${it}" } ?: t.id)

        ctx.print("looking for same ticket dirs")
        val ticketDirs = withContext(Dispatchers.IO) {
            Files.list(ticketDir)
                .filter { Files.isDirectory(it) }
                .filter { it.name.startsWith(t.id) }
                .collect(Collectors.toCollection(::HashSet))
        }
        ticketDirs.add(suggestedDir)

        val targetDir = if (overwrite) suggestedDir else ticketDirs.maxBy { it.name.length }
        ticketDirs.remove(targetDir)

        if (ticketDirs.isEmpty()) {
            if (!targetDir.isDirectory()) {
                ctx.print("creating $targetDir")
                targetDir.deleteIfExists()
                targetDir.createDirectory()
            }
        } else {
            ctx.print("merging $ticketDirs to $targetDir...")
            for (d in ticketDirs) {
                ctx.print("moving $d to $targetDir")
                deepMove(d, targetDir)
            }
        }

        ctx.print("done: $targetDir", PrintStatus.SUCCESS)
        ctx.cfg.path.cd.writeText(targetDir.toString())

        return true

    }
}


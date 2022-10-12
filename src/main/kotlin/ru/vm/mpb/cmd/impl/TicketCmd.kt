package ru.vm.mpb.cmd.impl

import ru.vm.mpb.cmd.Cmd
import ru.vm.mpb.cmd.CmdDesc
import ru.vm.mpb.cmd.ctx.CmdContext
import ru.vm.mpb.util.JiraTicket
import ru.vm.mpb.util.deepMove
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

private val DESC = CmdDesc(
    setOf("t", "ticket"),
    "make ticket dir",
    "<ticket id> [-o | --overwrite (overwrite directory using new description)] [description...]"
)

object TicketCmd: Cmd(DESC) {
    override fun execute(ctx: CmdContext) {

        val t = ctx.args.firstOrNull()?.let { JiraTicket.parse(ctx.cfg, it) } ?: printUsageAndExit()
        val overwrite = ctx.cfg.ticket.overwrite

        val desc = ctx.args.drop(1).joinToString(" ").replace(' ', '_').ifBlank { null }

        val ticketDir = ctx.cfg.ticket.dir.toPath()
        val suggestedDir = ticketDir.resolve(desc?.let { "${t.id}_${it}" } ?: t.id)

        val ticketDirs = HashSet<Path>()
        ticketDirs.add(suggestedDir)

        Files.list(ticketDir)
            .filter { Files.isDirectory(it) }
            .filter { it.name.startsWith(t.id) }
            .forEach(ticketDirs::add)

        val targetDir = if (overwrite) suggestedDir else ticketDirs.maxBy { it.name.length }
        ticketDirs.remove(targetDir)

        if (ticketDirs.isEmpty()) {
            if (!targetDir.isDirectory()) {
                targetDir.deleteIfExists()
                targetDir.createDirectory()
            }
        } else {
            for (d in ticketDirs) {
                deepMove(d, targetDir)
            }
        }
        ctx.print(targetDir)

    }
}

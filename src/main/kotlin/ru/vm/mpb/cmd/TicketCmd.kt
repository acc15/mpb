package ru.vm.mpb.cmd

import ru.vm.mpb.config.MpbConfig
import ru.vm.mpb.util.MessagePrinter
import ru.vm.mpb.util.deepMove
import ru.vm.mpb.util.parseJiraTicket
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

object TicketCmd: Cmd(
    setOf("t", "ticket"),
    "make ticket dir",
    "<ticket id> [-o | --overwrite (overwrite directory using new description)] [description...]"
) {
    override fun execute(cfg: MpbConfig) {

        val args = cfg.getCommonArgs()
        val t = args.firstOrNull()?.let { parseJiraTicket(cfg, it) } ?: printUsageAndExit()
        val overwrite = cfg.ticket.overwrite

        val desc = args.subList(1, args.size).joinToString(" ").replace(' ', '_').ifBlank { null }

        val ticketDir = cfg.ticket.dir.toPath()
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
        MessagePrinter(cfg).print(targetDir)

    }
}


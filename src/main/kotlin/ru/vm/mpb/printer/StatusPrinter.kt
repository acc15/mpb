package ru.vm.mpb.printer

import org.fusesource.jansi.Ansi
import org.fusesource.jansi.AnsiPrintStream
import ru.vm.mpb.ansi.filterJoin
import ru.vm.mpb.config.MpbConfig
import ru.vm.mpb.ansi.stripAnsi

class StatusPrinter(val out: AnsiPrintStream, val cfg: MpbConfig): Printer {

    private val lines: LinkedHashMap<String, Pair<String, List<Int>>> = cfg.projects.keys
        .associateWithTo(LinkedHashMap()) { "" to emptyList() }

    override fun print(data: PrintData) {

        val noAnsiLength = stripAnsi(data.msg).split(System.lineSeparator()).map { it.length }

        val li = lines[data.key]
        if (li == null) {
            lines[data.key] = data.msg to noAnsiLength
            out.println(data.msg)
            return
        }

        val w = cfg.output.getWidth(out.terminalWidth)
        val lineOffset = lines.flatMap { it.value.second }.sumOf { (it + w - 1) / w }
        lines[data.key] = data.msg to noAnsiLength

        out.println( Ansi().cursorUpLine(lineOffset)
            .eraseScreen(Ansi.Erase.FORWARD)
            .filterJoin(lines.values, System.lineSeparator()) { a, l ->
                if (l.second.isEmpty()) {
                    false
                } else {
                    a.a(l.first)
                    true
                }
            })

    }
}
package ru.vm.mpb.printer

import org.fusesource.jansi.Ansi
import org.fusesource.jansi.AnsiPrintStream
import ru.vm.mpb.config.OutputConfig
import ru.vm.mpb.jansi.stripAnsi

class StatusPrinter(val out: AnsiPrintStream, val cfg: OutputConfig): Printer {

    val lines = mutableListOf<Pair<String, List<Int>>>()
    val keyLines = mutableMapOf<String, Int>()

    override fun print(data: PrintData) {
        val printLine = keyLines.computeIfAbsent(data.key) { lines.size }
        val noAnsiLengths = stripAnsi(data.msg).split(System.lineSeparator()).map { it.length }
        if (printLine >= lines.size) {
            lines.add(data.msg to noAnsiLengths)
            out.println(data.msg)
        } else {
            val w = cfg.getWidth(out.terminalWidth)
            val lineOffset = lines.flatMap { it.second }.sumOf { (it + w - 1) / w }
            lines[printLine] = data.msg to noAnsiLengths
            out.println( Ansi().cursorUpLine(lineOffset)
                .eraseScreen(Ansi.Erase.FORWARD)
                .a(lines.joinToString(System.lineSeparator()) { it.first })
            )
        }
    }
}
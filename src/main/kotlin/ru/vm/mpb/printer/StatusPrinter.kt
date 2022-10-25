package ru.vm.mpb.printer

import org.fusesource.jansi.Ansi
import org.fusesource.jansi.AnsiPrintStream
import ru.vm.mpb.config.OutputConfig

class StatusPrinter(val out: AnsiPrintStream, val cfg: OutputConfig): Printer {

    val lines = mutableListOf<String>()
    val keyLines = mutableMapOf<String, Int>()

    override fun print(data: PrintData) {
        val printLine = keyLines.computeIfAbsent(data.key) { lines.size }
        val msg = data.format(cfg.monochrome)
        if (printLine >= lines.size) {
            lines.add(msg)
            out.println(msg)
        } else {
            val w = cfg.getWidth(out.terminalWidth)
            val lineOffset = lines.sumOf { (it.length + w - 1) / w }
            lines[printLine] = msg
            out.println( Ansi.ansi().cursorUpLine(lineOffset)
                .eraseScreen(Ansi.Erase.FORWARD)
                .a(lines.joinToString(System.lineSeparator()))
            )
        }
    }
}
package ru.vm.mpb.cmd.impl

import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.Consumer
import ru.vm.mpb.printer.PrintStatus

enum class BuildStatus(val action: String, val print: PrintStatus): Consumer {

    BUILDING("building", PrintStatus.MESSAGE),
    SKIP("skipped", PrintStatus.WARN),
    DONE("done", PrintStatus.SUCCESS),
    ERROR("failed", PrintStatus.ERROR);

    override fun apply(ansi: Ansi) {
        ansi.bold().a(action).boldOff()
    }

    companion object {
        fun valueOf(v: Boolean) = if (v) DONE else ERROR
        fun combine(a: BuildStatus, b: BuildStatus) = valueOf(a != ERROR && b != ERROR)
    }

}
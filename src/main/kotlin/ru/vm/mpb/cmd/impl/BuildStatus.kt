package ru.vm.mpb.cmd.impl

import ru.vm.mpb.printer.PrintStatus

enum class BuildStatus(val action: String, val printStatus: PrintStatus) {

    PENDING("pending", PrintStatus.MESSAGE),
    SKIP("skipped", PrintStatus.WARN),
    DONE("done", PrintStatus.SUCCESS),
    ERROR("failed", PrintStatus.ERROR);

    companion object {
        fun valueOf(v: Boolean) = if (v) DONE else ERROR
        fun combine(a: BuildStatus, b: BuildStatus) = valueOf(a != ERROR && b != ERROR)
    }

}
package ru.vm.mpb.cmd.impl

import ru.vm.mpb.printer.PrintStatus

enum class BuildStatus(val action: String, val printStatus: PrintStatus) {

    INIT("initializing", PrintStatus.MESSAGE),
    PENDING("pending", PrintStatus.MESSAGE),
    SKIP("skipped", PrintStatus.WARN),
    SUCCESS("done", PrintStatus.SUCCESS),
    ERROR("failed", PrintStatus.ERROR);

    companion object {
        fun fromBoolean(v: Boolean) = if (v) SUCCESS else ERROR
    }

}
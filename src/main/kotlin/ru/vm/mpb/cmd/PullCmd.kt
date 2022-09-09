package ru.vm.mpb.cmd

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.vm.mpb.config.MpbConfig
import ru.vm.mpb.executor.parallelExecutor
import ru.vm.mpb.util.MessagePrinter
import ru.vm.mpb.util.parseKeyArgs
import ru.vm.mpb.util.redirectErrorsIf
import ru.vm.mpb.util.runProcess

object PullCmd: Cmd(
    setOf("p", "pull"),
    "pull all repos",
    ""
) {
    override fun execute(cfg: MpbConfig, args: List<String>) {
        parallelExecutor(parseKeyArgs(cfg, args)) { p, _ ->
            val info = cfg.projects[p]!!
            val pp = MessagePrinter(cfg, p)
            withContext(Dispatchers.IO) {
                pp.print("pulling...")

                val success = runProcess(listOf("git", "pull", "--rebase"), info.dir, redirectErrorsIf(cfg.debug))
                pp.print(if (success) "done" else "error")
            }
        }
    }

}
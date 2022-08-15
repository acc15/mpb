package ru.vm.mpb.cmd

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.errors.TransportException
import ru.vm.mpb.config.MpbConfig
import ru.vm.mpb.executor.parallelExecutor
import ru.vm.mpb.util.makeGit
import ru.vm.mpb.util.parseKeyArgs

object PullCmd: Cmd(
    setOf("p", "pull"),
    "pulls all repos",
    ""
) {
    override fun execute(cfg: MpbConfig, args: List<String>) {
        parallelExecutor(parseKeyArgs(cfg, args)) { p, _ ->
            val info = cfg.projects[p]!!
            val pp = cfg.print.withPrefix(p)
            withContext(Dispatchers.IO) {
                val git = makeGit(info.dir)
                pp("pulling...")
                try {
                    git.pull().call()
                    pp("done")
                } catch (e: TransportException) {
                    pp("unable to pull: ${e.message}", e)
                }
            }
        }
    }

}
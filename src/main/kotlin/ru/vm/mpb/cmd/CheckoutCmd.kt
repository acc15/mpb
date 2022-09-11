package ru.vm.mpb.cmd

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.vm.mpb.config.MpbConfig
import ru.vm.mpb.config.ProjectConfig
import ru.vm.mpb.executor.parallelExecutor
import ru.vm.mpb.util.*

object CheckoutCmd: Cmd(
    setOf("c", "co", "checkout"),
    "checkout to specific commit/branch/tag",
    "[branch]"
) {

    private fun checkoutAndPull(cfg: MpbConfig, info: ProjectConfig, pp: MessagePrinter, branch: String) {

        pp.print("checkout to $branch")
        if (!runProcess(listOf("git", "checkout", branch), info.dir, redirectErrorsIf(cfg.debug))) {
            pp.print("unable to checkout to $branch")
            return
        }

        pp.print("pulling")
        if (!runProcess(listOf("git", "pull"), info.dir, redirectErrorsIf(cfg.debug))) {
            pp.print("unable to pull")
            return
        }

    }

    override fun execute(cfg: MpbConfig) {

        parallelExecutor(cfg.getActiveProjectArgs()) { p, list ->

            val info = cfg.projects[p]!!
            val branch = if (list.isEmpty()) cfg.getDefaultBranch(p) else list[0]

            val pp = MessagePrinter(cfg, p)
            withContext(Dispatchers.IO) {

                pp.print("fetching all remotes...")
                if (!runProcess(listOf("git", "fetch", "--all"), info.dir, redirectErrorsIf(cfg.debug))) {
                    pp.print("unable to fetch")
                    return@withContext
                }

                val hasChanges = !runProcess(listOf("git", "diff", "--quiet"), info.dir)
                if (hasChanges) {
                    pp.print("stashing")
                    if (!runProcess(listOf("git", "stash"), info.dir, redirectErrorsIf(cfg.debug))){
                        pp.print("unable to stash")
                        return@withContext
                    }
                }

                checkoutAndPull(cfg, info, pp, branch)
                if (hasChanges) {
                    pp.print("restoring stash")
                    if (!runProcess(listOf("git", "stash", "pop"), info.dir, redirectErrorsIf(cfg.debug))) {
                        pp.print("unable to restore from stash")
                        return@withContext
                    }
                }

                pp.print("done")

            }

        }

    }
}

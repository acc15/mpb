package ru.vm.mpb.cmd

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.errors.TransportException
import ru.vm.mpb.config.MpbConfig
import ru.vm.mpb.executor.parallelExecutor
import ru.vm.mpb.util.MessagePrinter
import ru.vm.mpb.util.makeGit
import ru.vm.mpb.util.parseKeyArgs

object CheckoutCmd: Cmd(
    setOf("c", "co", "checkout"),
    "checkout to specific commit/branch/tag",
    "[branch]"
) {
    override fun execute(cfg: MpbConfig, args: List<String>) {

        parallelExecutor(parseKeyArgs(cfg, args)) { p, list ->

            val info = cfg.projects[p]!!
            val branch = if (list.isEmpty()) cfg.getDefaultBranch(p) else list[0]

            val pp = MessagePrinter(cfg, p)
            withContext(Dispatchers.IO) {

                val git = makeGit(info.dir)
                for (remote in git.remoteList().call()) {
                    try {
                        pp.print("fetching ${remote.name}...")
                        git.fetch().setRemote(remote.name).call()
                    } catch (e: TransportException) {
                        pp.print("unable to fetch ${remote.name}: ${e.message}", e)
                    }
                }

                val status = git.status().call()
                val stash = if (status.hasUncommittedChanges()) {
                    pp.print("stashing")
                    git.stashCreate().call()
                } else null

                pp.print("checkout to $branch")
                git.checkout().setName(branch).call()

                pp.print("pulling...")
                try {
                    git.pull().call()
                } catch (e: TransportException) {
                    pp.print("unable to pull: ${e.message}", e)
                }
                if (stash != null) {
                    pp.print("restoring stash")
                    git.stashApply().setStashRef(stash.name).call()
                }
            }
            pp.print("done")

        }

    }
}

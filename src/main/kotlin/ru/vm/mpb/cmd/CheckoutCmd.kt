package ru.vm.mpb.cmd

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.api.errors.TransportException
import ru.vm.mpb.config.MpbConfig
import ru.vm.mpb.executor.parallelExecutor
import ru.vm.mpb.util.parseProjectArgs
import ru.vm.mpb.util.prefixPrinter

object CheckoutCmd: Cmd(
    setOf("c", "co", "checkout"),
    "checkout to specific commit/branch/tag",
    "[branch]"
) {
    override fun execute(cfg: MpbConfig, args: List<String>) {

        val pargs = parseProjectArgs(cfg, args)
        parallelExecutor(cfg) { p ->

            val info = cfg.projects[p]!!
            val list = pargs[p]
            val branch = if (list.isEmpty()) cfg.getDefaultBranch(p) else list[0]

            val pp = prefixPrinter(System.out, p)
            withContext(Dispatchers.IO) {

                val git = Git(FileRepositoryBuilder().setWorkTree(info.dir.toFile()).findGitDir().build())
                for (remote in git.remoteList().call()) {
                    try {
                        pp("fetching ${remote.name}...")
                        git.fetch().setRemote(remote.name).call()
                    } catch (e: TransportException) {
                        pp("unable to fetch ${remote.name}: ${e.message}")
                    }
                }

                val status = git.status().call()
                val stash = if (status.hasUncommittedChanges()) {
                    pp("stashing")
                    git.stashCreate().call()
                } else null

                pp("checkout to $branch")
                git.checkout().setName(branch).call()

                pp("pulling...")
                try {
                    git.pull().call()
                } catch (e: TransportException) {
                    pp("unable to pull: ${e.message}")
                }
                if (stash != null) {
                    pp("restoring stash")
                    git.stashApply().setStashRef(stash.name).call()
                }
            }
            pp("done")

        }

    }
}

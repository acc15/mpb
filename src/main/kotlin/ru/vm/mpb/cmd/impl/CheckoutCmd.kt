package ru.vm.mpb.cmd.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.vm.mpb.cmd.CmdDesc
import ru.vm.mpb.cmd.ParallelCmd
import ru.vm.mpb.cmd.ctx.ProjectContext

private val DESC = CmdDesc(
    setOf("c", "co", "checkout"),
    "checkout to specific commit/branch/tag",
    "[branch]"
)

object CheckoutCmd: ParallelCmd(DESC) {

    private fun checkoutAndPull(ctx: ProjectContext) {
        val branch = ctx.args.firstOrNull() ?: ctx.cfg.getDefaultBranch(ctx.key)

        ctx.print("checkout to $branch")
        if (!ctx.exec("git", "checkout", branch)) {
            ctx.print("unable to checkout to $branch")
            return
        }

        ctx.print("pulling")
        if (!ctx.exec("git", "pull")) {
            ctx.print("unable to pull")
            return
        }

    }

    override suspend fun parallelExecute(ctx: ProjectContext) = withContext(Dispatchers.IO) {

        ctx.print("fetching all remotes...")
        if (!ctx.exec("git", "fetch", "--all")) {
            ctx.print("unable to fetch")
            return@withContext
        }

        val hasChanges = !ctx.exec("git", "diff", "--quiet")
        if (hasChanges) {
            ctx.print("stashing")
            if (!ctx.exec("git", "stash")) {
                ctx.print("unable to stash")
                return@withContext
            }
        }

        checkoutAndPull(ctx)
        if (hasChanges) {
            ctx.print("restoring stash")
            if (!ctx.exec("git", "stash", "pop")) {
                ctx.print("unable to restore from stash")
                return@withContext
            }
        }

        ctx.print("done")

    }

}

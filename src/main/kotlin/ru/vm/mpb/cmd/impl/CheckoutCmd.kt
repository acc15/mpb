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

    private fun resolveBranch(ctx: ProjectContext): String {

        val pb = ctx.info.branch
        val gb = ctx.cfg.branch

        val subject = ctx.args.firstOrNull() ?: return pb.default ?: gb.default ?: "master"

        val patterns = pb.filters + gb.filters
        if (patterns.isEmpty()) {
            return subject
        }

        val branches = ctx.exec("git", "branch", "-r").lines().map { it.substring(2) }
        for (p in patterns) {

            val regex = Regex(p.regex.replace("\${branch}", Regex.escape(subject)))
            val matches = branches
                .mapNotNull { regex.matchEntire(it) }
                .map { it.groupValues.getOrNull(1) ?: it.value }

            when {
                p.index < 0 && matches.isNotEmpty() -> return matches.last()
                p.index >= 0 && p.index < matches.size -> return matches[p.index]
            }
        }

        return subject
    }

    private fun checkoutAndPull(ctx: ProjectContext): Boolean {

        val branch = resolveBranch(ctx)

        ctx.print("checkout to $branch")
        if (!ctx.exec("git", "checkout", branch).success()) {
            ctx.print("unable to checkout to $branch")
            return false
        }

        ctx.print("pulling")
        if (!ctx.exec("git", "pull", "--rebase").success()) {
            ctx.print("unable to pull")
            return false
        }
        return true
    }

    override suspend fun parallelExecute(ctx: ProjectContext): Boolean = withContext(Dispatchers.IO) {

        ctx.print("fetching all remotes...")
        if (!ctx.exec("git", "fetch", "--all").success()) {
            ctx.print("unable to fetch")
            return@withContext false
        }

        val hasChanges = !ctx.exec("git", "diff", "--quiet").success()
        if (hasChanges) {
            ctx.print("stashing")
            if (!ctx.exec("git", "stash").success()) {
                ctx.print("unable to stash")
                return@withContext false
            }
        }

        val success = checkoutAndPull(ctx)
        if (hasChanges) {
            ctx.print("restoring stash")
            if (!ctx.exec("git", "stash", "pop").success()) {
                ctx.print("unable to restore from stash")
                return@withContext false
            }
        }

        if (success) {
            ctx.print("done")
        }
        success

    }

}

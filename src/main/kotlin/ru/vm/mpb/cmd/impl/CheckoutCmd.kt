package ru.vm.mpb.cmd.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.vm.mpb.cmd.CmdDesc
import ru.vm.mpb.cmd.ParallelCmd
import ru.vm.mpb.cmd.ctx.ProjectContext
import ru.vm.mpb.printer.PrintStatus


object CheckoutCmd: ParallelCmd {

    override val desc = CmdDesc(
        listOf("c", "co", "checkout"),
        "checkout to specific commit/branch/tag",
        "[branch]"
    )

    private fun resolveBranch(ctx: ProjectContext): String {
        val pb = ctx.info.branch
        val gb = ctx.cfg.branch

        val subject = ctx.args.firstOrNull() ?: return pb.default ?: gb.default ?: "master"

        val patterns = pb.patterns + gb.patterns
        if (patterns.isEmpty()) {
            return subject
        }

        val branches = ctx.exec("git", "branch", "-r").lines().map { it.substring(2) }
        return patterns.firstNotNullOfOrNull { it.findBranch(subject, branches) } ?: subject
    }

    private fun checkoutAndPull(ctx: ProjectContext, branch: String): Boolean {
        ctx.print("checkout to $branch")
        if (!ctx.exec("git", "checkout", branch).success()) {
            ctx.print("unable to checkout to $branch", PrintStatus.ERROR)
            return false
        }

        ctx.print("pulling")
        if (!ctx.exec("git", "pull", "--rebase").success()) {
            ctx.print("unable to pull", PrintStatus.ERROR)
            return false
        }
        return true
    }

    override suspend fun parallelExecute(ctx: ProjectContext): Boolean = withContext(Dispatchers.IO) {

        ctx.print("fetching all remotes...")
        if (!ctx.exec("git", "fetch", "--all").success()) {
            ctx.print("unable to fetch", PrintStatus.ERROR)
            return@withContext false
        }

        val hasChanges = !ctx.exec("git", "diff", "--quiet").success()
        if (hasChanges) {
            ctx.print("stashing")
            if (!ctx.exec("git", "stash").success()) {
                ctx.print("unable to stash", PrintStatus.ERROR)
                return@withContext false
            }
        }

        val branch = resolveBranch(ctx)
        val success = checkoutAndPull(ctx, branch)
        if (hasChanges) {
            ctx.print("restoring stash")
            if (!ctx.exec("git", "stash", "pop").success()) {
                ctx.print("unable to restore from stash", PrintStatus.ERROR)
                return@withContext false
            }
        }

        if (success) {
            ctx.print(ctx.ansi.a("on ").bold().a(branch).reset(), PrintStatus.SUCCESS)
        }
        success

    }

}

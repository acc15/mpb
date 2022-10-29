package ru.vm.mpb.cmd.impl

import kotlinx.coroutines.*
import ru.vm.mpb.cmd.Cmd
import ru.vm.mpb.cmd.CmdDesc
import ru.vm.mpb.cmd.ctx.CmdContext
import ru.vm.mpb.cmd.ctx.ProjectContext
import ru.vm.mpb.printer.PrintStatus
import ru.vm.mpb.progress.IndeterminateProgressBar
import ru.vm.mpb.util.bfs
import ru.vm.mpb.util.dfs
import ru.vm.mpb.util.prettyString
import ru.vm.mpb.util.transferAndTrackProgress
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

typealias BuildInfoMap = Map<String, BuildInfo>

object BuildCmd: Cmd {

    override val desc = CmdDesc(
        listOf("b", "build"),
        "build all projects",
        "[build profile]"
    )

    override suspend fun execute(ctx: CmdContext): Boolean {
        if (ctx.cfg.build.isEmpty()) {
            ctx.print("build configuration not specified", PrintStatus.ERROR)
            return false
        }

        if (findCycles(ctx)) {
            return false
        }

        val roots = getRootKeys(ctx)
        val bi = createBuildInfoMap(ctx)

        ctx.print("building...")

        val result = coroutineScope { launchBuilds(this, roots, null, ctx, bi) }
        if (result) {
            ctx.print("success", PrintStatus.SUCCESS)
        } else {
            ctx.print("error", PrintStatus.ERROR)
        }
        return result
    }

    private suspend fun launchBuilds(
        scope: CoroutineScope,
        keys: Iterable<String>,
        parentKey: String?,
        ctx: CmdContext,
        bi: BuildInfoMap
    ): Boolean {
        val tasks = mutableListOf<Deferred<Boolean>>()
        for (k in keys) {
            val b = bi.getValue(k)
            if (parentKey != null) {
                b.pendingDeps.remove(parentKey)
            }
            if (b.pendingDeps.isNotEmpty()) {
                continue
            }
            if (!b.status.compareAndSet(BuildStatus.INIT, BuildStatus.PENDING)) {
                continue
            }
            tasks += scope.async { build(scope, ctx.projectContext(k), bi) }
        }
        return tasks.awaitAll().all { it }
    }

    suspend fun build(scope: CoroutineScope, ctx: ProjectContext, bi: BuildInfoMap): Boolean {
        val args = ctx.cfg.args.active[ctx.key]
        val status = if (args != null) {
            runBuild(ctx, args)
        } else {
            ctx.print("skipped", PrintStatus.WARN)
            BuildStatus.SKIP
        }

        val b = bi.getValue(ctx.key)
        if (!b.status.compareAndSet(BuildStatus.PENDING, status)) {
            ctx.print("can't update build status to $status", PrintStatus.WARN)
        }

        return if (status == BuildStatus.SUCCESS) {
            launchBuilds(scope, b.dependants, ctx.key, ctx.cmd, bi)
        } else {
            updateChildrenStatus(ctx, bi)
            status == BuildStatus.SKIP
        }
    }

    private fun updateChildrenStatus(ctx: ProjectContext, bi: BuildInfoMap) {
        val b = bi.getValue(ctx.key)
        val status = b.status.get()
        bfs(b.dependants, { bi.getValue(it).dependants }, onNode = {
            if (!bi.getValue(it).status.compareAndSet(BuildStatus.INIT, status)) {
                return@bfs false
            }
            ctx.print("${status.action} due to ${ctx.key} is ${status.action}", status.printStatus, it)
            true
        })
    }

    private suspend fun runBuild(ctx: ProjectContext, args: List<String>): BuildStatus = withContext(Dispatchers.IO) {
        val command = ctx.build.makeCommand(args.firstOrNull())
        val buildStart = System.nanoTime()

        ctx.info.log.parentFile.mkdirs()
        val process = ctx.exec(command)
            .redirectTo(ProcessBuilder.Redirect.PIPE)
            .env(ctx.build.env)
            .start()

        val msg = "building: ${command.joinToString(" ")}"
        ctx.info.log.outputStream().use {
            val progress = IndeterminateProgressBar()
            transferAndTrackProgress(process.inputStream to it, process.errorStream to it) {
                ctx.print(ctx.ansi.a(msg).a(' ').apply(progress.update(20)))
            }
        }

        val success = process.waitFor() == 0

        val duration = Duration.ofNanos(System.nanoTime() - buildStart)
        BuildStatus.fromBoolean(success).also {
            ctx.print("${it.action} in ${duration.prettyString}", it.printStatus)
        }
    }

    private fun createBuildInfoMap(ctx: CmdContext): BuildInfoMap = ctx.cfg.projects.mapValues { (k, i) ->
        BuildInfo(
            i.deps.associateWithTo(ConcurrentHashMap()) {},
            ctx.cfg.projects.filter { e -> e.value.deps.contains(k) }.keys
        )
    }

    private fun getRootKeys(ctx: CmdContext) = ctx.cfg.projects.filter { e -> e.value.deps.isEmpty() }.keys

    private fun findCycles(ctx: CmdContext): Boolean {
        val projects = ctx.cfg.projects
        val cycles = mutableListOf<List<String>>()
        dfs(projects.keys, { projects.getValue(it).deps }, onCycle = cycles::add)
        return if (cycles.isEmpty()) false else {
            ctx.print("dependency cycles detected: ${cycles.joinToString { it.joinToString(" -> ") }}",
                PrintStatus.ERROR)
            true
        }
    }

}

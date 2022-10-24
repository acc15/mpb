package ru.vm.mpb.cmd.impl

import kotlinx.coroutines.*
import ru.vm.mpb.cmd.Cmd
import ru.vm.mpb.cmd.CmdDesc
import ru.vm.mpb.cmd.ctx.CmdContext
import ru.vm.mpb.cmd.ctx.ProjectContext
import ru.vm.mpb.config.DEFAULT_KEY
import ru.vm.mpb.util.*
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

enum class BuildStatus {
    INIT,
    PENDING,
    SKIP,
    SUCCESS,
    ERROR
}

data class BuildInfo(
    val pendingDeps: ConcurrentHashMap<String, Unit>,
    val dependants: Set<String>,
    var status: AtomicReference<BuildStatus> = AtomicReference(BuildStatus.INIT)
)

typealias BuildInfoMap = Map<String, BuildInfo>

private val DESC = CmdDesc(
    setOf("b", "build"),
    "build all projects",
    "[build profile]"
)

object BuildCmd: Cmd(DESC) {

    override suspend fun execute(ctx: CmdContext): Boolean {
        if (ctx.cfg.build.isEmpty()) {
            ctx.print("build configuration not specified")
            return false
        }

        if (findCycles(ctx)) {
            return false
        }

        val roots = getRootKeys(ctx)
        if (roots.isEmpty()) {
            ctx.print("no root projects found - please check configuration")
            return false
        }

        val bi: BuildInfoMap = createBuildInfoMap(ctx)
        return launchBuilds(roots, null, ctx, bi)
    }

    private suspend fun launchBuilds(
        keys: Iterable<String>,
        parentKey: String?,
        ctx: CmdContext,
        bi: BuildInfoMap
    ): Boolean = coroutineScope {
        keys.map { it to bi.getValue(it) }
            .onEach { (_, b) ->
                if (parentKey != null) {
                    b.pendingDeps.remove(parentKey)
                }
            }
            .filter { (_, b) -> b.pendingDeps.isEmpty() }
            .filter { (_, b) -> b.status.compareAndSet(BuildStatus.INIT, BuildStatus.PENDING) }
            .map { (k, _) -> async { build(ctx.projectContext(k), bi) } }
            .awaitAll()
            .all { it }
    }

    suspend fun build(ctx: ProjectContext, bi: BuildInfoMap): Boolean {
        val args = ctx.cfg.activeArgs[ctx.key]
        val status = if (args != null) {
            withContext(Dispatchers.IO) {
                runBuild(ctx, args)
            }
        } else {
            ctx.print("skipped")
            BuildStatus.SKIP
        }

        val b = bi.getValue(ctx.key)
        if (!b.status.compareAndSet(BuildStatus.PENDING, status)) {
            ctx.print("can't update build status to $status")
        }

        if (status == BuildStatus.SUCCESS) {
            return launchBuilds(b.dependants, ctx.key, ctx.cmd, bi)
        }

        updateChildrenStatus(ctx, bi)
        return status == BuildStatus.SKIP
    }

    private fun updateChildrenStatus(ctx: ProjectContext, bi: BuildInfoMap) {
        val b = bi.getValue(ctx.key)
        val status = b.status.get()
        bfs(b.dependants, { bi.getValue(it).dependants }, onNode = {
            if (!bi.getValue(it).status.compareAndSet(BuildStatus.INIT, status)) {
                return@bfs false
            }

            val action = if (status == BuildStatus.SKIP) "skipped" else "failed"
            ctx.print("$action due to ${ctx.key} is $action", key = it)
            true
        })
    }

    private fun runBuild(ctx: ProjectContext, args: List<String>): BuildStatus {
        val command = args.firstOrNull()?.let { ctx.build.profiles[it] } ?: ctx.build.profiles.getValue(DEFAULT_KEY)

        ctx.print("building: ${command.joinToString(" ")}")
        val buildStart = System.nanoTime()

        ctx.info.logFile.parentFile.mkdirs()
        val success = ctx.exec(command)
            .redirectTo(ctx.info.logFile)
            .env(ctx.build.env)
            .success()

        val duration = Duration.ofNanos(System.nanoTime() - buildStart)
        return if (success) {
            ctx.print("success in ${duration.prettyPrint()}")
            BuildStatus.SUCCESS
        } else {
            ctx.print("error in ${duration.prettyPrint()}")
            BuildStatus.ERROR
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
            ctx.print("cycles detected: $cycles")
            true
        }
    }

}

package ru.vm.mpb.cmd.impl

import kotlinx.coroutines.*
import ru.vm.mpb.cmd.Cmd
import ru.vm.mpb.cmd.CmdDesc
import ru.vm.mpb.cmd.ctx.CmdContext
import ru.vm.mpb.cmd.ctx.ProjectContext
import ru.vm.mpb.config.DEFAULT_KEY
import ru.vm.mpb.util.*
import java.nio.file.Files
import java.nio.file.Path
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

fun findCycles(key: String, bi: BuildInfoMap, handler: (List<String>) -> Unit): Boolean {
    var hasCycles = false
    dfs(key, { bi.getValue(it).dependants }, onCycle = {
        hasCycles = true
        handler(it)
        false
    })
    return hasCycles
}

fun traverseProjects(keys: Set<String>, bi: BuildInfoMap, handler: (String) -> Unit) {
    bfsFirstVisitOnly(keys, { bi.getValue(it).dependants }, onNode = {
        handler(it)
        true
    })
}

fun updateChildrenStatus(ctx: ProjectContext, bi: BuildInfoMap, status: BuildStatus, callback: (String) -> Unit) {
    traverseProjects(setOf(ctx.key), bi) {
        val expectStatus = if (it == ctx.key) BuildStatus.PENDING else BuildStatus.INIT
        if (bi.getValue(it).status.compareAndSet(expectStatus, status)) {
            callback(it)
        }
    }
}

object BuildCmd: Cmd(
    CmdDesc(
    setOf("b", "build"),
    "build all projects",
    "[build profile]"
)
) {

    override suspend fun execute(ctx: CmdContext): Boolean = coroutineScope {
        if (ctx.cfg.build.isEmpty()) {
            ctx.print("build configuration not specified")
            return@coroutineScope false
        }

        val bi: BuildInfoMap = ctx.cfg.projects.mapValues { (k, i) ->
            BuildInfo(
                i.deps.associateWithTo(ConcurrentHashMap()) {},
                ctx.cfg.projects.filter { e -> e.value.deps.contains(k) }.keys
            )
        }

        val roots = ctx.cfg.projects.filter { e -> e.value.deps.isEmpty() }.keys
        if (roots.isEmpty()) {
            ctx.print("invalid configuration, no root projects found")
            return@coroutineScope false
        }

        for (r in roots) {
            if (findCycles(r, bi) { ctx.print("cycle detected: ${it.joinToString(", ")}") }) {
                return@coroutineScope false
            }
        }

        for (r in roots) {
            bi.getValue(r).status.set(BuildStatus.PENDING)
            launch {
                build(this, ctx.projectContext(r), bi)
            }
        }

        return@coroutineScope true
    }

    suspend fun build(scope: CoroutineScope, ctx: ProjectContext, bi: BuildInfoMap) {

        val i = bi.getValue(ctx.key)

        val a = ctx.cfg.activeArgs[ctx.key]
        if (a == null) {
            updateChildrenStatus(ctx, bi, BuildStatus.SKIP) {
                ctx.cmd.print(if (it == ctx.key) "skipped" else "skipped due to ${ctx.key} is skipped", key = it)
            }
            return
        }

        val profile = a.firstOrNull() ?: DEFAULT_KEY

        val buildConfig = ctx.cfg.build[ctx.info.build]!!
        val command = buildConfig.profiles[profile] ?: buildConfig.profiles[DEFAULT_KEY]!!

        val status = withContext(Dispatchers.IO) {
            ctx.print("building: ${command.joinToString(" ")}")
            val buildStart = System.nanoTime()

            val logDir = Files.createDirectories(Path.of("log"))
            val logFile = logDir.resolve("${ctx.key}.log").toFile()

            val success = ctx.exec(command)
                .redirectTo(logFile)
                .env(buildConfig.env)
                .success()

            val duration = Duration.ofNanos(System.nanoTime() - buildStart)
            if (success) {
                ctx.print("success in ${duration.prettyPrint()}")
                BuildStatus.SUCCESS
            } else {
                ctx.print("error in ${duration.prettyPrint()}")
                BuildStatus.ERROR
            }
        }

        if (status != BuildStatus.SUCCESS) {
            updateChildrenStatus(ctx, bi, status) {
                if (it != ctx.key) {
                    ctx.cmd.print("failed due to ${ctx.key} is failed", key = it)
                }
            }
            return
        }

        for (d in i.dependants) {
            val di = bi.getValue(d)
            di.pendingDeps.remove(ctx.key)
            if (di.pendingDeps.isNotEmpty()) {
                continue
            }

            if (!di.status.compareAndSet(BuildStatus.INIT, BuildStatus.PENDING)) {
                continue
            }

            scope.launch {
                build(scope, ctx.cmd.projectContext(d), bi)
            }
        }

    }
}

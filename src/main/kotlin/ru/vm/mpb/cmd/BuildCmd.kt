package ru.vm.mpb.cmd

import kotlinx.coroutines.*
import ru.vm.mpb.config.DEFAULT_KEY
import ru.vm.mpb.config.MpbConfig
import ru.vm.mpb.util.*
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.exitProcess

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

data class BuildParams(
    val cfg: MpbConfig,
    val args: Map<String, List<String>>,
    val bi: Map<String, BuildInfo> = cfg.projects.mapValues { (k, i) ->
        BuildInfo(
            i.deps.associateWithTo(ConcurrentHashMap()) {},
            cfg.projects.filter { e -> e.value.deps.contains(k) }.keys
        )
    }
) {
    fun getProjectConfig(k: String) = cfg.projects[k]!!
    fun getBuildInfo(k: String): BuildInfo = bi[k]!!
}

fun findCycles(key: String, bp: BuildParams, handler: (List<String>) -> Unit) {
    dfs(key, { bp.getBuildInfo(it).dependants }, onCycle = {
        handler(it)
        false
    })
}

fun traverseProjects(keys: Set<String>, bp: BuildParams, handler: (String) -> Unit) {
    bfsFirstVisitOnly(keys, { bp.getBuildInfo(it).dependants }, onNode = {
        handler(it)
        true
    })
}

object BuildCmd: Cmd(
    setOf("b", "build"),
    "build all projects",
    "[build profile | skip - to skip building]"
) {

    override fun execute(cfg: MpbConfig) {

        val bp = BuildParams(cfg, cfg.getActiveProjectArgs())
        val roots = cfg.projects.filter { e -> e.value.deps.isEmpty() }.keys

        val p = MessagePrinter(cfg)

        if (roots.isEmpty()) {
            p.print("invalid configuration, no root projects found")
            exitProcess(1)
        }

        for (r in roots) {
            findCycles(r, bp) {
                p.print("cycle detected: ${it.joinToString(", ")}")
                exitProcess(1)
            }
        }

        runBlocking(Dispatchers.Default) {
            for (k in roots) {
                bp.getBuildInfo(k).status.set(BuildStatus.PENDING)
                launch {
                    build(this, k, bp)
                }
            }
        }

    }

    suspend fun build(scope: CoroutineScope, k: String, bp: BuildParams) {

        val i = bp.getBuildInfo(k)
        val pp = MessagePrinter(bp.cfg, k)

        val a = bp.args[k]
        if (a == null) {
            traverseProjects(setOf(k), bp) {
                val expectStatus = if (it == k) BuildStatus.PENDING else BuildStatus.INIT
                if (bp.getBuildInfo(it).status.compareAndSet(expectStatus, BuildStatus.SKIP)) {
                    pp.print(if (it == k) "skipped" else "skipped due to $k is skipped", prefix = it)
                }
            }
            return
        }

        val profile = a.firstOrNull() ?: DEFAULT_KEY
        val pcfg = bp.getProjectConfig(k)

        val buildConfig = bp.cfg.build[pcfg.build]!!
        val command = buildConfig.profiles[profile] ?: buildConfig.profiles[DEFAULT_KEY]!!

        val status = withContext(Dispatchers.IO) {
            pp.print("building: ${command.joinToString(" ")}")
            val buildStart = System.nanoTime()

            val logDir = Files.createDirectories(Path.of("log"))
            val logFile = logDir.resolve("$k.log").toFile()

            val success = runProcess(command, pcfg.dir) { it
                    .redirectOutput(logFile)
                    .redirectError(logFile)
                    .environment().putAll(buildConfig.env)
            }

            val duration = Duration.ofNanos(System.nanoTime() - buildStart)
            if (success) {
                pp.print("success in ${duration.prettyPrint()}")
                BuildStatus.SUCCESS
            } else {
                pp.print("error in ${duration.prettyPrint()}")
                BuildStatus.ERROR
            }

        }

        if (status != BuildStatus.SUCCESS) {
            traverseProjects(setOf(k), bp) {
                val expectStatus = if (it == k) BuildStatus.PENDING else BuildStatus.INIT
                if (bp.getBuildInfo(it).status.compareAndSet(expectStatus, status) && it != k) {
                    pp.print("failed due to $k is failed", prefix = it)
                }
            }
            return
        }

        for (d in i.dependants) {
            val di = bp.getBuildInfo(d)
            di.pendingDeps.remove(k)
            if (di.pendingDeps.isNotEmpty()) {
                continue
            }

            if (!di.status.compareAndSet(BuildStatus.INIT, BuildStatus.PENDING)) {
                continue
            }

            scope.launch {
                build(scope, d, bp)
            }
        }

    }
}

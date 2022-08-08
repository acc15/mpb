package ru.vm.mpb.cmd

import kotlinx.coroutines.*
import ru.vm.mpb.config.MpbConfig
import ru.vm.mpb.util.*
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.exitProcess

const val SKIP_BUILD_PROFILE = "skip"
const val DEFAULT_BUILD_PROFILE = "default"

enum class BuildStatus {
    BUILD,
    PENDING,
    SKIP,
    SUCCESS,
    ERROR
}

data class BuildInfo(
    val pendingDeps: ConcurrentHashMap<String, Unit>,
    val dependants: Set<String>,
    var status: AtomicReference<BuildStatus> = AtomicReference(BuildStatus.BUILD)
)

data class BuildParams(
    val cfg: MpbConfig,
    val args: KeyArgs,
    val bi: Map<String, BuildInfo> = cfg.projects.mapValues { (k, i) ->
        BuildInfo(
            i.deps.associateWithTo(ConcurrentHashMap()) {},
            cfg.projects.filter { e -> e.value.deps.contains(k) }.keys
        )
    }
) {
    fun getProjectConfig(k: String) = cfg.projects[k]!!
    fun getBuildInfo(k: String): BuildInfo = bi[k]!!
    fun getProjectArgs(k: String): List<String> = args[k]
}

fun findCycles(key: String, bp: BuildParams, handler: (List<String>) -> Unit) {
    dfs(key, { bp.getBuildInfo(it).dependants }, onCycle = { it ->
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

    override fun execute(cfg: MpbConfig, args: List<String>) {

        val bp = BuildParams(cfg, parseProjectArgs(cfg, args))
        val roots = cfg.projects.filter { e -> e.value.deps.isEmpty() }.keys

        if (roots.isEmpty()) {
            println("invalid configuration, no root projects found")
            exitProcess(1)
        }

        for (r in roots) {
            findCycles(r, bp) {
                println("cycle detected: ${it.joinToString(", ")}")
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
        val pp = PrefixPrinter(System.out, k)
        val a = bp.getProjectArgs(k)

        if (a.isNotEmpty() && a[0] == SKIP_BUILD_PROFILE) {
            traverseProjects(setOf(k), bp) {
                val expectStatus = if (it == k) BuildStatus.PENDING else BuildStatus.BUILD
                if (bp.getBuildInfo(it).status.compareAndSet(expectStatus, BuildStatus.SKIP)) {
                    if (it == k) {
                        pp.withPrefix(it)("skipped")
                    } else {
                        pp.withPrefix(it)("skipped (due to $k is skipped)")
                    }
                }
            }
            return
        }

        val profile = if (a.isNotEmpty()) a[0] else DEFAULT_BUILD_PROFILE
        val pcfg = bp.getProjectConfig(k)

        val commands = pcfg.build ?: bp.cfg.build
        val command = commands[profile] ?: commands[DEFAULT_BUILD_PROFILE]!!

        val status = withContext(Dispatchers.IO) {
            pp("building...")
            val buildStart = System.nanoTime()

            val logDir = Files.createDirectories(Path.of("log"))
            val logFile = logDir.resolve("$k.log").toFile()
            val exitCode = ProcessBuilder(command)
                .directory(pcfg.dir.toFile())
                .redirectOutput(logFile)
                .redirectError(logFile)
                .start()
                .waitFor()

            val duration = Duration.ofNanos(System.nanoTime() - buildStart)
            if (exitCode == 0) {
                pp("success (in ${duration.prettyPrint()})")
                BuildStatus.SUCCESS
            } else {
                pp("failed (in ${duration.prettyPrint()})")
                BuildStatus.ERROR
            }

        }

        if (status != BuildStatus.SUCCESS) {
            traverseProjects(setOf(k), bp) {
                val expectStatus = if (it == k) BuildStatus.PENDING else BuildStatus.BUILD
                if (bp.getBuildInfo(it).status.compareAndSet(expectStatus, status) && it != k) {
                    pp.withPrefix(it)("failed (due to $k is failed)")
                }
            }
            return
        }

        for (d in i.dependants) {
            val di = bp.getBuildInfo(d)
            di.pendingDeps.remove(k)
            if (!di.pendingDeps.isEmpty()) {
                continue
            }

            if (!di.status.compareAndSet(BuildStatus.BUILD, BuildStatus.PENDING)) {
                continue
            }

            scope.launch {
                build(scope, d, bp)
            }
        }

    }
}

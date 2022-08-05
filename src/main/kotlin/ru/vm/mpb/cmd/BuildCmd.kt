package ru.vm.mpb.cmd

import kotlinx.coroutines.*
import ru.vm.mpb.config.MpbConfig
import ru.vm.mpb.util.bfsOnce
import ru.vm.mpb.util.dfs
import ru.vm.mpb.util.parseProjectArgs
import ru.vm.mpb.util.prefixPrinter
import kotlin.system.exitProcess

const val SKIP_BUILD_PROFILE = "skip"
const val DEFAULT_BUILD_PROFILE = "default"

enum class BuildStatus {
    BUILD,
    SKIP,
    OK,
    ERROR
}

class BuildInfo(
    val deps: Set<String>,
    val dependants: MutableSet<String> = mutableSetOf(),
    var status: BuildStatus = BuildStatus.BUILD

) {
    override fun toString(): String = "BuildInfo(status=${status},dependants=${dependants})"
}

fun propagateStatus(k: String, build: Map<String, BuildInfo>) {
    dfs(k,
        { build[it]!!.dependants.iterator() },
        onEdge = { s, t ->
            val si = build[s]!!
            val ti = build[t]!!
            ti.status = maxOf(ti.status, si.status)
            true
        },
        onCycle = {
            println("cycle detected: ${it.joinToString(", ")}")
            exitProcess(1)
        }
    )
}

fun traverseProjects(keys: Set<String>, bi: Map<String, BuildInfo>, handler: (String) -> Unit) {
    bfsOnce(keys, { bi[it]!!.dependants.iterator() }, { bi[it]!!.deps.size }, onNode = {
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

        val bi = mutableMapOf<String, BuildInfo>()
        val roots = mutableSetOf<String>()

        val pargs = parseProjectArgs(cfg, args)

        for ((p, info) in cfg.projects) {
            if (info.deps.isEmpty()) {
                roots.add(p)
            }
            val status = pargs[p].let { if (it.isNotEmpty() && it[0] == SKIP_BUILD_PROFILE) BuildStatus.SKIP else BuildStatus.BUILD }
            bi.computeIfAbsent(p) { BuildInfo(info.deps) }.status = status
            for (d in info.deps) {
                bi.computeIfAbsent(d) { BuildInfo(cfg.projects[d]!!.deps) }.dependants.add(p)
            }
        }

        if (roots.isEmpty()) {
            println("invalid configuration, no root projects found")
            exitProcess(1)
        }

        for (r in roots) {
            propagateStatus(r, bi)
        }

        println("build plan: ")
        traverseProjects(roots, bi) { prefixPrinter(System.out, it)("${bi[it]!!.status}") }
        println()

        val buildDeps = bi.filter { e -> e.value.status == BuildStatus.BUILD }.mapValues { it.value.deps.toMutableSet() }
        runBlocking(Dispatchers.Default) {
            for (k in roots) {
                launch {
                    build(k, bi)
                }
            }
        }

    }

    suspend fun build(k: String, bi: Map<String, BuildInfo>) {

        val pp = prefixPrinter(System.out, k)
        if (bi[k]!!.status == BuildStatus.SKIP) {
            traverseProjects(setOf(k), bi) { pp("skipping...") }
            return
        }

        val status = withContext(Dispatchers.IO) {
            pp("building...")
            delay(1000)
            pp("finished")
            BuildStatus.OK
        }

        if (status == BuildStatus.ERROR) {
            traverseProjects(setOf(k), bi) { pp("build failed") }
            return
        }

        for (d in bi[k]!!.dependants) {



        }

    }
}

package ru.vm.mpb.cmd

import ru.vm.mpb.config.MpbConfig
import ru.vm.mpb.util.dfs
import ru.vm.mpb.util.parseProjectArgs
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
    val dependants: MutableSet<String> = mutableSetOf(),
    var status: BuildStatus = BuildStatus.BUILD

) {
    override fun toString(): String = "BuildInfo(status=${status},dependants=${dependants})"
}

fun propagateStatus(k: String, build: Map<String, BuildInfo>) {
    dfs(k,
        { build[it]!!.dependants.iterator() },
        onEdge = { s, t ->
            build[t]!!.status = build[s]!!.status
            true
        },
        onCycle = {
            println("cycle detected: ${it.joinToString(", ")}")
            exitProcess(1)
        }
    )
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
            bi.computeIfAbsent(p) { BuildInfo() }.status = status

            for (d in info.deps) {
                bi.computeIfAbsent(d) { BuildInfo() }.dependants.add(p)
            }
        }

        println(bi)
        println(roots)

        if (roots.isEmpty()) {
            println("invalid configuration, no root projects found")
            exitProcess(1)
        }
        for (r in roots) {
            propagateStatus(r, bi)
        }

        bfs(roots, )



/*
        for (e in cfg.projects) {

            val p = e.key
            val info = e.value

            val a = pargs[p]
            val profile = if (a.isNotEmpty()) a[0] else DEFAULT_BUILD_PROFILE

            val commands = info.build ?: cfg.build
            val command = commands[profile] ?: commands[DEFAULT_BUILD_PROFILE]!!

            println(e.key + " " + command)
        }
*/


    }
}

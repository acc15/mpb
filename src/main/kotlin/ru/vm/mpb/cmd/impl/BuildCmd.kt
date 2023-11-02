package ru.vm.mpb.cmd.impl

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import ru.vm.mpb.ansi.boldAppender
import ru.vm.mpb.ansi.join
import ru.vm.mpb.cmd.Cmd
import ru.vm.mpb.cmd.CmdDesc
import ru.vm.mpb.cmd.ctx.CmdContext
import ru.vm.mpb.cmd.ctx.ProjectContext
import ru.vm.mpb.io.RedirectingInputStream
import ru.vm.mpb.io.SynchronizedOutputStream
import ru.vm.mpb.printer.PrintStatus
import ru.vm.mpb.progress.BuildProgress
import ru.vm.mpb.util.dfs
import ru.vm.mpb.util.environment
import ru.vm.mpb.util.pretty
import ru.vm.mpb.util.redirectBoth
import java.nio.file.Files
import java.time.Duration
import kotlin.io.path.outputStream

object BuildCmd: Cmd {

    override val desc = CmdDesc(
        listOf("b", "build"),
        "build all projects",
        "[build command]"
    )

    override suspend fun execute(ctx: CmdContext): Boolean {
        if (ctx.cfg.args.active.isEmpty()) {
            ctx.print("no one project is active", PrintStatus.ERROR)
            return false
        }

        if (ctx.cfg.build.isEmpty()) {
            ctx.print("build configuration not specified", PrintStatus.ERROR)
            return false
        }

        if (findCycles(ctx)) {
            return false
        }

        ctx.print(ctx.ansi.apply(BuildStatus.BUILDING).a("..."))

        val start = System.nanoTime()
        val status = buildActive(ctx)
        val duration = Duration.ofNanos(System.nanoTime() - start)
        ctx.print(ctx.ansi.apply(status).a(" in ").apply(duration.pretty), status.print)
        return status != BuildStatus.ERROR
    }

    private suspend fun buildActive(ctx: CmdContext): BuildStatus = coroutineScope {
        val activeProjectsWithDeps = ctx.cfg.run {
            args.active.mapValues { (k) ->
                projects[k]!!.deps.filter { args.active.containsKey(it) }.toCollection(LinkedHashSet())
            }
        }

        val channels = activeProjectsWithDeps
            .filterValues { it.isNotEmpty() }
            .mapValues { Channel<BuildEvent>(Channel.BUFFERED) }

        activeProjectsWithDeps.keys.map {
            async {
                build(ctx.projectContext(it), activeProjectsWithDeps, channels)
            }
        }.awaitAll().reduce(BuildStatus::combine)
    }

    private suspend fun build(
        ctx: ProjectContext,
        projects: Map<String, Set<String>>,
        channels: Map<String, Channel<BuildEvent>>
    ): BuildStatus {

        val send = multiSend(projects
            .filterValues { it.contains(ctx.key) }
            .map { channels.getValue(it.key) })

        var status = BuildStatus.BUILDING
        val recv = channels[ctx.key]
        if (recv != null && !waitDependencies(ctx, projects.getValue(ctx.key), recv, send)) {
            status = BuildStatus.SKIP
        }

        if (status == BuildStatus.BUILDING) {
            status = runBuild(ctx, ctx.args)
            if (status != BuildStatus.DONE) {
                send(BuildEvent(ctx.key, ctx.key, status))
            }
        }

        send(BuildEvent(ctx.key, ctx.key, BuildStatus.DONE))
        return status
    }

    private suspend fun runBuild(ctx: ProjectContext, args: List<String>): BuildStatus = withContext(Dispatchers.IO) {

        val buildProgress = BuildProgress.init(ctx)

        val command = ctx.build.getCommandLine(args.firstOrNull())
        val buildStart = System.nanoTime()

        Files.createDirectories(ctx.info.log.parent)
        val process = ctx.exec(command)
            .redirectBoth(ProcessBuilder.Redirect.PIPE)
            .environment(ctx.build.env)
            .start()

        val msg = ctx.ansi.apply(BuildStatus.BUILDING).a(": ").join(command, " ")
        SynchronizedOutputStream(ctx.info.log.outputStream()).use { out ->
            buildProgress.process(
                RedirectingInputStream(process.inputStream, out),
                RedirectingInputStream(process.errorStream, out)
            ) {
                ctx.print(ctx.ansi(msg).a(' ').apply(it))
            }
        }

        val status = BuildStatus.valueOf(process.waitFor() == 0)
        val duration = Duration.ofNanos(System.nanoTime() - buildStart)
        ctx.print(ctx.ansi.apply(status).a(" in ").apply(duration.pretty), status.print)

        status
    }

    private suspend fun waitDependencies(
        ctx: ProjectContext,
        deps: Set<String>,
        recv: ReceiveChannel<BuildEvent>,
        send: suspend (BuildEvent) -> Unit
    ): Boolean {

        val pending = LinkedHashSet(deps)
        val skipReasons = LinkedHashMap<BuildStatus, LinkedHashSet<String>>()
        while (pending.isNotEmpty()) {

            if (!ctx.skipped && skipReasons.isEmpty()) {
                ctx.print(ctx.ansi.a("waiting for ").join(pending, ", ", boldAppender()),
                    PrintStatus.MESSAGE)
            }

            val event = recv.receive()
            if (event.status == BuildStatus.DONE) {
                pending.remove(event.key)
                continue
            }

            if (ctx.skipped) {
                continue
            }

            skipReasons.computeIfAbsent(event.status) { LinkedHashSet() }.add(event.reason)
            ctx.print(ctx.ansi.a("skipped due to ").join(skipReasons.entries, " and ") { a, it ->
                a.join(it.value, ", ", boldAppender()).a(" is ").apply(it.key)
            }, PrintStatus.WARN)

            send(BuildEvent(ctx.key, event.reason, event.status))

        }
        return skipReasons.isEmpty()
                || (!ctx.info.propagateSkip && skipReasons.keys.singleOrNull() == BuildStatus.SKIP)
    }

    private fun findCycles(ctx: CmdContext): Boolean {
        val projects = ctx.cfg.projects
        val cycles = mutableListOf<List<String>>()
        dfs(projects.keys, { projects.getValue(it).deps }, onCycle = cycles::add)
        if (cycles.isEmpty()) {
            return false
        }

        ctx.print(ctx.ansi.a("dependency cycles detected: ")
            .join(cycles, ", ") { a, p -> a.join(p, " -> ", boldAppender()) }, PrintStatus.ERROR)
        return true
    }

    private fun <T> multiSend(channels: Iterable<SendChannel<T>>): suspend (T) -> Unit = {
        for (c in channels) {
            c.send(it)
        }
    }

}

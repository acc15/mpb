package ru.vm.mpb.cmd.impl

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.reduce
import ru.vm.mpb.cmd.Cmd
import ru.vm.mpb.cmd.CmdDesc
import ru.vm.mpb.cmd.ctx.CmdContext
import ru.vm.mpb.cmd.ctx.ProjectContext
import ru.vm.mpb.printer.PrintStatus
import ru.vm.mpb.progress.IndeterminateProgressBar
import ru.vm.mpb.util.dfs
import ru.vm.mpb.util.prettyString
import ru.vm.mpb.util.transferAndTrackProgress
import java.time.Duration

object BuildCmd: Cmd {

    override val desc = CmdDesc(
        listOf("b", "build"),
        "build all projects",
        "[build profile]"
    )

    override suspend fun execute(ctx: CmdContext): Boolean {
        if (ctx.cfg.projects.isEmpty()) {
            ctx.print("no one project configured", PrintStatus.ERROR)
            return false
        }

        if (ctx.cfg.build.isEmpty()) {
            ctx.print("build configuration not specified", PrintStatus.ERROR)
            return false
        }

        if (findCycles(ctx)) {
            return false
        }

        ctx.print("building...")

        val start = System.nanoTime()
        val status = buildAll(ctx)
        val duration = Duration.ofNanos(System.nanoTime() - start)
        ctx.print("${status.action} in ${duration.prettyString}", status.printStatus)
        return status != BuildStatus.ERROR
    }

    private suspend fun buildAll(ctx: CmdContext): BuildStatus = coroutineScope {
        val channels = ctx.cfg.projects
            .filterValues { it.deps.isNotEmpty() }
            .mapValues { Channel<BuildEvent>(Channel.BUFFERED) }

        ctx.cfg.projects.keys.map {
            async {
                build(ctx.projectContext(it), channels)
            }
        }.awaitAll().reduce(BuildStatus::combine)
    }

    private suspend fun build(ctx: ProjectContext, channels: Map<String, Channel<BuildEvent>>): BuildStatus {
        val send = multiSend(ctx.cfg.projects
            .filterValues { it.deps.contains(ctx.key) }
            .map { channels.getValue(it.key) })

        var status = BuildStatus.PENDING
        if (ctx.skipped) {
            ctx.print("skipped", PrintStatus.WARN)
            status = BuildStatus.SKIP
            send(BuildEvent(ctx.key, ctx.key, status))
        }

        val recv = channels[ctx.key]
        if (recv != null && !waitDependencies(ctx, recv, send)) {
            status = BuildStatus.SKIP
        }

        if (status == BuildStatus.PENDING) {
            status = runBuild(ctx, ctx.args)
            if (status != BuildStatus.DONE) {
                send(BuildEvent(ctx.key, ctx.key, status))
            }
        }

        send(BuildEvent(ctx.key, ctx.key, BuildStatus.DONE))
        return status
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

        val status = BuildStatus.valueOf(process.waitFor() == 0)
        val duration = Duration.ofNanos(System.nanoTime() - buildStart)
        ctx.print("${status.action} in ${duration.prettyString}", status.printStatus)

        status
    }

    private suspend fun waitDependencies(
        ctx: ProjectContext,
        recv: ReceiveChannel<BuildEvent>,
        send: suspend (BuildEvent) -> Unit
    ): Boolean {

        val pending = LinkedHashSet(ctx.info.deps)
        val skipReasons = LinkedHashMap<BuildStatus, LinkedHashSet<String>>()
        while (pending.isNotEmpty()) {

            if (!ctx.skipped && skipReasons.isEmpty()) {
                ctx.print("waiting for $pending", PrintStatus.MESSAGE)
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
            val reasonText = skipReasons.map { "${it.value} is ${it.key.action}" }.joinToString(" and ")
            ctx.print("skipped due to $reasonText", PrintStatus.WARN)

            send(BuildEvent(ctx.key, event.reason, event.status))

        }
        return skipReasons.isEmpty()
    }

    private fun findCycles(ctx: CmdContext): Boolean {
        val projects = ctx.cfg.projects
        val cycles = mutableListOf<List<String>>()
        dfs(projects.keys, { projects.getValue(it).deps }, onCycle = cycles::add)
        if (cycles.isEmpty()) {
            return false
        }

        ctx.print("dependency cycles detected: ${cycles.joinToString { it.joinToString(" -> ") }}", PrintStatus.ERROR)
        return true
    }

    private fun <T> multiSend(channels: Iterable<SendChannel<T>>): suspend (T) -> Unit = {
        for (c in channels) {
            c.send(it)
        }
    }

}

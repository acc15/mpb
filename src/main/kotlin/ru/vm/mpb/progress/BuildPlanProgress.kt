package ru.vm.mpb.progress

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.fusesource.jansi.Ansi
import ru.vm.mpb.cmd.ctx.ProjectContext
import ru.vm.mpb.cmd.impl.BuildStatus
import ru.vm.mpb.progressbar.ColoredProgressBar
import ru.vm.mpb.util.readFully
import java.io.InputStream

class BuildPlanProgress(val ctx: ProjectContext, val progress: ColoredProgressBar): BuildProgress {

    private val cfg = ctx.build.progress
    private val plan = HashSet<String>()

    override suspend fun init(): Unit =  withContext(Dispatchers.IO) {
        ctx.print(ctx.ansi.apply(BuildStatus.BUILDING).a(": execution plan"))

        plan.clear()
        val process = ctx.exec(cfg.cmd)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .start()
        cfg.plan.findAllMatches(process.inputReader().lineSequence(), plan::add)
        process.waitFor()
    }

    override suspend fun process(
        inp: InputStream,
        err: InputStream,
        onProgress: (Ansi.Consumer) -> Unit
    ) = withContext(Dispatchers.IO) {

        // discarding error stream
        launch {
            readFully(err) { _, _ -> isActive }
        }

        val remaining = HashSet<String>(plan)
        cfg.build.findAllMatches(inp.bufferedReader().lineSequence()) { match ->

            remaining.remove(match)

            onProgress(progress.apply {
                current = plan.size - remaining.size
                total = plan.size
                text = match
            }.update())

        }

    }
}
package ru.vm.mpb.progress

import org.fusesource.jansi.Ansi.Consumer
import ru.vm.mpb.cmd.ctx.ProjectContext
import ru.vm.mpb.progressbar.ColoredProgressBar
import ru.vm.mpb.progressbar.IndeterminateProgressBar
import java.io.InputStream

interface BuildProgress {

    suspend fun init()
    suspend fun process(inp: InputStream, err: InputStream, onProgress: (Consumer) -> Unit)

    companion object {

        suspend fun init(ctx: ProjectContext) = get(ctx).also { it.init() }

        fun get(ctx: ProjectContext): BuildProgress {
            val width = 50
            return if (ctx.build.progress.cmd.isEmpty())
                IndeterminateBuildProgress(IndeterminateProgressBar(width))
            else
                BuildPlanProgress(ctx, ColoredProgressBar(width))
        }
    }

}
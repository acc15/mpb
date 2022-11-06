package ru.vm.mpb.cmd.impl

import kotlinx.coroutines.delay
import ru.vm.mpb.cmd.Cmd
import ru.vm.mpb.cmd.CmdDesc
import ru.vm.mpb.cmd.ctx.CmdContext
import ru.vm.mpb.progressbar.*

object TestCmd: Cmd {

    override val desc = CmdDesc(listOf("x"), "test", "<type>")

    private val map = mapOf("progress" to ::progress, "indeterminate" to ::indeterminate)

    override suspend fun execute(ctx: CmdContext): Boolean {
        val type = ctx.args.first()
        map.getValue(type)(ctx)
        return true
    }

    suspend fun progress(ctx: CmdContext) {
        val p = ColoredProgressBar(50, 0, 1000)
        for (i in 0..1000) {
            ctx.print(ctx.ansi.apply(p.apply { current = i; text = "${i / 10}.${i % 10}%" }.update()))
            delay(50)
        }
    }

    suspend fun indeterminate(ctx: CmdContext) {
        val p = IndeterminateProgressBar(50)
        for (i in 0..1000) {
            ctx.print(ctx.ansi.apply(p.update()))
            delay(50)
        }
    }
}
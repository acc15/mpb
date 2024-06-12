package ru.vm.mpb.progress

import org.fusesource.jansi.Ansi
import ru.vm.mpb.progressbar.ProgressBar

class ProgressBarTester<T: ProgressBar>(val bar: T, val max: Int = 100, val min: Int = 0) {

    fun test(changer: T.(cur: Int) -> Unit = {}) {
        val out = System.out
        for (i in min .. max) {
            bar.apply { changer(this, i) }
            bar.update()
            out.print(Ansi.ansi().a('\r').apply(bar))
            out.flush()
            Thread.sleep(100)
        }
        out.println()
    }

}
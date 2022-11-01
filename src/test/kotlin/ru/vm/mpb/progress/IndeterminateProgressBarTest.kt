package ru.vm.mpb.progress

import org.junit.jupiter.api.Test
import kotlin.test.Ignore

class IndeterminateProgressBarTest {

    @Test
    @Ignore
    fun animation() {
        val p = IndeterminateProgressBar()
        val width = 20
        for (i in 0 until 100) {
            print("\r" + p.update(width))
            System.out.flush()
            Thread.sleep(100)
        }
        println()
    }
}
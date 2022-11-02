package ru.vm.mpb.progress

import kotlin.test.*

class ColoredProgressBarTest {


    @Test
    // @Ignore
    fun animation() {
        ProgressBarTester(ColoredProgressBar(50), 100).test { i ->
            amount = i
            total = 100
            message = "$i%"
        }
    }

}
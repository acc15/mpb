package ru.vm.mpb.progress

import ru.vm.mpb.progressbar.ColoredProgressBar
import kotlin.test.*

class ColoredProgressBarTest {


    @Test
    // @Ignore
    fun animation() {
        ProgressBarTester(ColoredProgressBar(50), 100).test { i ->
            amount = i
            total = 100
            text = "$i%"
        }
    }

}
package ru.vm.mpb.progress

import ru.vm.mpb.progressbar.ColoredProgressBar
import kotlin.test.*

class ColoredBuildProgressBarTest {


    @Test
    @Ignore
    fun animation() {
        ProgressBarTester(ColoredProgressBar(50), 100).test { i ->
            current = i
            total = 100
            text = "$i%"
        }
    }

}
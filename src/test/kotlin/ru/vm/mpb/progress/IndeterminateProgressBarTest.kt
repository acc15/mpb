package ru.vm.mpb.progress

import ru.vm.mpb.progressbar.IndeterminateProgressBar
import kotlin.test.*

class IndeterminateProgressBarTest {

    @Test
    @Ignore
    fun animation() {
        ProgressBarTester(IndeterminateProgressBar(20)).test()
    }
}
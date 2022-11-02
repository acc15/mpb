package ru.vm.mpb.progress

import kotlin.test.*

class IndeterminateProgressBarTest {

    @Test
    @Ignore
    fun animation() {
        ProgressBarTester(IndeterminateProgressBar(20)).test()
    }
}
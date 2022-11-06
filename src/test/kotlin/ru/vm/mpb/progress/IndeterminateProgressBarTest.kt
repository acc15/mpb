package ru.vm.mpb.progress

import ru.vm.mpb.progressbar.IndeterminateProgressBar
import kotlin.test.*

class IndeterminateProgressBarTest {

    @Test
    @Ignore
    fun animation() {
        ProgressBarTester(IndeterminateProgressBar(20)).test()
    }


//
//    @Test
//    fun testInterpolateRgb() {
//        val p = IndeterminateProgressBar(20)
//
//        for (i in 0..10) {
//            System.out.printf("%06x vs %06x%n ", interpolateRgb(0xff00, 0xff, i, 10), p.interpolateRgb(0xff00, 0xff, i, 10))
//        }
//    }
}
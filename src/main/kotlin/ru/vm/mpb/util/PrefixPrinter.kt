package ru.vm.mpb.util

import java.io.PrintStream

class PrefixPrinter(val ps: PrintStream, val prefix: String) {
    operator fun invoke(str: String) = ps.println("[$prefix] $str")
    fun withPrefix(other: String) = PrefixPrinter(ps, other)
}

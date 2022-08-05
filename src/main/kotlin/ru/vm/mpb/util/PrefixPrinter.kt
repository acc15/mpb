package ru.vm.mpb.util

import java.io.PrintStream

fun prefixPrinter(ps: PrintStream, prefix: String): (String) -> Unit = { ps.println("[$prefix] $it") }

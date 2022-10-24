package ru.vm.mpb.printer

data class PrintData(
    val msg: Any?,
    val ex: Throwable? = null,
    val key: String = "",
)
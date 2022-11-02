package ru.vm.mpb.progress

import org.fusesource.jansi.Ansi.Consumer

interface ProgressBar: Consumer {
    fun update(): ProgressBar
}
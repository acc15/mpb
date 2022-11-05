package ru.vm.mpb.progressbar

import org.fusesource.jansi.Ansi.Consumer

interface ProgressBar: Consumer {
    fun update(): ProgressBar
}
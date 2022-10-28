package ru.vm.mpb.progress

import kotlinx.coroutines.*
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean

suspend fun withIndeterminateProgress(vararg streams: InputStream, callback: (IndeterminateProgressBar) -> Unit) {
    withContext(Dispatchers.Default) {
        val changeFlag = AtomicBoolean(false)
        launch {
            val progress = IndeterminateProgressBar()
            while (isActive) {
                if (changeFlag.getAndSet(false)) {
                    callback(progress)
                }
                delay(100)
            }
        }
        withContext(Dispatchers.IO) {
            for (s in streams) {
                launch {
                    @Suppress("BlockingMethodInNonBlockingContext")
                    while (s.read() >= 0) {
                        changeFlag.set(true)
                    }
                }
            }
        }
    }
}
package ru.vm.mpb.progress

import kotlinx.coroutines.*
import org.fusesource.jansi.Ansi
import ru.vm.mpb.progressbar.ProgressBar
import ru.vm.mpb.io.readFully
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean

class IndeterminateBuildProgress(
    private val progressBar: ProgressBar
): BuildProgress {

    override suspend fun init() {
    }

    override suspend fun process(
        inp: InputStream,
        err: InputStream,
        onProgress: (Ansi.Consumer) -> Unit
    ) = coroutineScope {

        val changeFlag = AtomicBoolean(false)
        val progressJob = launch {
            while (isActive) {
                if (changeFlag.getAndSet(false)) {
                    onProgress(progressBar.update())
                }
                delay(50)
            }
        }

        withContext(Dispatchers.IO) {
            launch {
                readFully(err) { _, _ -> changeFlag.set(true); isActive }
            }
            readFully(inp) { _, _ -> changeFlag.set(true); isActive }
        }

        progressJob.cancelAndJoin()
    }
}
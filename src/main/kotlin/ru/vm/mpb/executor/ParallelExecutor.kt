package ru.vm.mpb.executor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import ru.vm.mpb.config.MpbConfig
import ru.vm.mpb.util.KeyArgs
import java.nio.file.Path

fun parallelExecutor(args: KeyArgs, impl: suspend (String, List<String>) -> Unit) {
    runBlocking(Dispatchers.Default) {
        for ((p, v) in args) {
            launch {
                impl(p, v)
            }
        }
    }
}


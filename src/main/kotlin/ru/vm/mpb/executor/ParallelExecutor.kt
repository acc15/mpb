package ru.vm.mpb.executor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import ru.vm.mpb.config.MpbConfig
import java.nio.file.Path

fun parallelExecutor(cfg: MpbConfig, impl: suspend (String) -> Unit) {
    runBlocking(Dispatchers.Default) {
        for (p in cfg.projects.keys) {
            launch {
                impl(p)
            }
        }
    }
}


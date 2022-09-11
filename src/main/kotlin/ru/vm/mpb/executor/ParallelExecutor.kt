package ru.vm.mpb.executor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun parallelExecutor(args: Map<String, List<String>>, impl: suspend (String, List<String>) -> Unit) {
    runBlocking(Dispatchers.Default) {
        for ((p, v) in args) {
            launch {
                impl(p, v)
            }
        }
    }
}


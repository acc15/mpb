package ru.vm.mpb.cmd.impl

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

data class BuildInfo(
    val pendingDeps: ConcurrentHashMap<String, Unit>,
    val dependants: Set<String>,
    val status: AtomicReference<BuildStatus> = AtomicReference(BuildStatus.INIT)
)
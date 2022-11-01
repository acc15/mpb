package ru.vm.mpb.cmd.impl

data class BuildEvent(
    val key: String,
    val reason: String,
    val status: BuildStatus
)
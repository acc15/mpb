package ru.vm.mpb.config.state

typealias ConfigStateMutator = (Any) -> Unit

abstract class ConfigImmutableState(
    private val mutator: ConfigStateMutator
): ConfigState {
    override fun set(value: Any) {
        mutator(value)
    }
}
package ru.vm.mpb.config.state

abstract class AbstractConfig(private val mutator: ConfigMutator): Config {
    override fun set(other: Any?) {
        mutator(other)
    }
}
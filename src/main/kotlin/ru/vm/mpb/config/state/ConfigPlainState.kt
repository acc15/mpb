package ru.vm.mpb.config.state

class ConfigPlainState(override val value: Any?, mutate: ConfigStateMutator): ConfigImmutableState(mutate) {

    override val indices: IntRange = IntRange.EMPTY
    override val keys: Set<String> = emptySet()

    override fun get(key: String) = toConfigState(null) { newState ->
        set(putNonNull(mutableMapOf(), "" to value, key to newState))
    }

    override fun get(index: Int) = toConfigState(null) { newState ->
        set(putNonNull(mutableListOf(), 0 to value, index to newState))
    }

    override fun add(value: Any) {
        set(if (this.value == null) value else mutableListOf(this.value, value))
    }

    override fun merge(value: Any) {
        set(value)
    }

    override val list: List<Any?> get() = if (value != null) listOf(value) else emptyList()
    override val plain: Any? get() = value
}
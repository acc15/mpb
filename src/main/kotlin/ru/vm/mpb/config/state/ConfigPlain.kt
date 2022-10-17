package ru.vm.mpb.config.state

class ConfigPlain(
    override val value: Any?,
    mutate: ConfigMutator
): Config(mutate) {

    override fun get(key: String) = toConfig(null) { newState ->
        set(putNonNull(mutableMapOf(), "" to value, key to newState))
    }

    override fun get(index: Int) = toConfig(null) { newState ->
        set(putNonNull(mutableListOf(), 0 to value, index to newState))
    }

    override fun add(value: Any) {
        set(if (this.value == null) value else mutableListOf(this.value, value))
    }

    override fun merge(value: Any) {
        set(value)
    }

    override val list: List<Any?> get() = if (value != null) listOf(value) else emptyList()
    override val map: Map<String, Any> get() = putNonNull(mutableMapOf(), "" to value)
    override val plain: Any? get() = value

}
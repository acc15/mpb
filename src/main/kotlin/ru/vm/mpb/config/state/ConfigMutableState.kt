package ru.vm.mpb.config.state

class ConfigMutableState(override var value: Any? = null): ConfigState {
    val state: ConfigState
        get() = toConfigState(value, this::set)

    override val indices: IntRange get() = state.indices
    override val keys: Set<String> get() = state.keys

    override fun get(key: String) = state.get(key)
    override fun get(index: Int) = state.get(index)
    override fun add(value: Any) = state.add(value)
    override fun merge(value: Any) = state.merge(value)
    override fun set(value: Any) {
        this.value = value
    }

    override val list: List<Any?> get() = state.list
    override val plain: Any? get() = state.plain
}
package ru.vm.mpb.config.state

class ConfigRoot(
    override var value: Any? = null,
    val mutator: ConfigMutator = {}
): Config {

    var state = Config.of(value, this::set)
        private set

    override fun get(key: String) = state.get(key)
    override fun get(index: Int) = state.get(index)
    override fun add(other: Any?) = state.add(other)
    override fun merge(other: Any?) = state.merge(other)
    override fun set(other: Any?) {
        mutator(other)
        this.value = other
        this.state = Config.of(other, this::set)
    }

    override val list: List<Any?> get() = state.list
    override val map: Map<String, Any> get() = state.map
    override val plain: Any? get() = state.plain

}
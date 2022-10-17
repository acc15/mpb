package ru.vm.mpb.config.state

class ConfigRoot(
    override var value: Any? = null,
    mutator: ConfigMutator = {}
): Config(mutator) {

    var state: Config = toConfig(value, this::set)
        private set

    override fun get(key: String) = state.get(key)
    override fun get(index: Int) = state.get(index)
    override fun add(value: Any) = state.add(value)
    override fun merge(value: Any) = state.merge(value)
    override fun set(value: Any) {
        super.set(value)
        this.value = value
        this.state = toConfig(this.value, this::set)
    }

    override val list: List<Any?> get() = state.list
    override val map: Map<String, Any> get() = state.map
    override val plain: Any? get() = state.plain

}
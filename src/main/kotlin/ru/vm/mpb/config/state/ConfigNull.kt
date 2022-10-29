package ru.vm.mpb.config.state

class ConfigNull(mutator: ConfigMutator): Config(mutator) {
    override val value: Any? get() = null

    override fun get(key: String) = of(null) { if (it != null) set(applyValues(LinkedHashMap(), key to it)) }
    override fun get(index: Int) = of(null) { if (it != null) set(applyValues(ArrayList(), index to it)) }
    override fun add(value: Any?) = set(value)

    override val list: List<Any?> get() = emptyList()
    override val map: Map<String, Any> get() = emptyMap()
    override val plain: Any? get() = null
}
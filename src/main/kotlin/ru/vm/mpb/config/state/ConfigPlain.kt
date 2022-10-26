package ru.vm.mpb.config.state

class ConfigPlain(
    override val value: Any,
    mutator: ConfigMutator
): Config(mutator) {

    override fun get(key: String) = of(null) { set(putNonNull(mutableMapOf("" to value), key to it)) }
    override fun get(index: Int) = of(null) { set(putNonNull(mutableListOf(value), index to it)) }
    override fun add(value: Any?) = set(mutableListOf(this.value, value))

    override val list: List<Any?> get() = listOf(value)
    override val map: Map<String, Any> get() = mapOf("" to value)
    override val plain: Any get() = value

}
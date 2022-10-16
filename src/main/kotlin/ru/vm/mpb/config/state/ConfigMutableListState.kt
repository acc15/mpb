package ru.vm.mpb.config.state

class ConfigMutableListState(
    override val list: MutableList<Any?>,
    mutator: ConfigStateMutator
): ConfigImmutableState(mutator) {

    override val value: Any = list

    override val indices: IntRange get() = list.indices
    override val keys: Set<String> = emptySet()

    override fun get(key: String) = toConfigState(null) { set(mutableMapOf("" to list, key to it)) }
    override fun get(index: Int) = toConfigState(list.getOrNull(index)) { putNonNull(list, index to it) }

    override fun add(value: Any) {
        list.add(value)
    }

    override fun merge(value: Any) {
        mapValueByType(value,
            { map -> ConfigMutableMapState(map.toMutableMap(), this::set).also { set(it) }.merge(list) },
            { list -> list.withIndex().filter { it.value != null }.forEach { get(it.index).merge(it.value!!) } },
            { plain -> set(plain!!) }
        )
    }

    override val plain: Any?
        get() = list.firstOrNull()
}
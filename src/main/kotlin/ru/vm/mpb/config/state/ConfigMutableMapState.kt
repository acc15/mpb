package ru.vm.mpb.config.state

class ConfigMutableMapState(
    val map: MutableMap<String, Any>,
    mutator: ConfigStateMutator
): ConfigImmutableState(mutator) {

    override val value: MutableMap<String, Any> = map

    override val indices: IntRange = IntRange.EMPTY
    override val keys: Set<String> get() = map.keys

    override fun get(key: String) = toConfigState(map[key]) { map[key] = it }
    override fun get(index: Int) = get("").get(index)

    override fun add(value: Any) {
        get("").add(value)
    }

    override fun merge(value: Any) {
        mapValueByType(value,
            { map -> map.entries.forEach { get(it.key).merge(it.value) } },
            { list -> get("").merge(list) },
            { plain -> get("").merge(plain!!) }
        )
    }

    override val list: List<Any?> get() = get("").list
    override val plain: Any? get() = get("").plain
}

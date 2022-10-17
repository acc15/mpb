package ru.vm.mpb.config.state

class ConfigMap(
    override val map: Map<String, Any>,
    mutator: ConfigMutator
): Config(mutator) {

    override val value: Map<String, Any> = map

    override fun get(key: String) = of(map[key]) { mutateMap(map)[key] = it }
    override fun get(index: Int) = get("").get(index)

    override fun add(value: Any) {
        get("").add(value)
    }

    override fun merge(value: Any) {
        mapValueByType(value,
            { map ->
                ConfigRoot(this.map, this::set).also { mut ->
                    map.entries.forEach { e -> mut.get(e.key).merge(e.value) }
                }
            },
            { list -> get("").merge(list) },
            { plain -> get("").merge(plain!!) }
        )
    }

    override val list: List<Any?> get() = get("").list
    override val plain: Any? get() = get("").plain

    private fun <K, V> mutateMap(map: Map<K, V>): LinkedHashMap<K, V> =
        map as? LinkedHashMap<K, V> ?: LinkedHashMap(map).also(this::set)
}

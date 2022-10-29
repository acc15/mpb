package ru.vm.mpb.config.state

class ConfigMap(
    override val map: Map<String, Any>,
    mutator: ConfigMutator
): Config(mutator) {

    override val value: Map<String, Any> = map

    override fun get(key: String) = of(map[key]) {
        val map = mutateMap()
        if (it != null) {
            map[key] = it
        } else {
            map.remove(key)
        }
    }

    override fun get(index: Int) = get("").get(index)
    override fun add(value: Any?) = get("").add(value)

    override fun merge(value: Any?) {
        mapValueByType(value,
            { map ->
                val mut = ConfigRoot(this.map, this::set)
                for ((k, v) in map) {
                    mut.get(k).merge(v)
                }
            },
            { list -> get("").merge(list) },
            { plain -> get("").merge(plain) },
            { }
        )
    }

    override val list: List<Any?> get() = get("").list
    override val plain: Any? get() = get("").plain

    private fun mutateMap(): LinkedHashMap<String, Any> =
        map as? LinkedHashMap<String, Any> ?: LinkedHashMap(map).also(this::set)
}

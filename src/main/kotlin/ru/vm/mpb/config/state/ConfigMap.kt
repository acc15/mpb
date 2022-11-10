package ru.vm.mpb.config.state

class ConfigMap(
    override val map: Map<String, Any>,
    mutator: ConfigMutator
): AbstractConfig(mutator) {

    override val value: Map<String, Any> = map

    override fun get(key: String) = Config.of(map[key]) { Config.applyValues(mutableMap, key to it) }
    override fun get(index: Int) = get("").get(index)
    override fun add(other: Any?) = get("").add(other)

    override fun merge(other: Any?) {
        Config.mapByType(other,
            { map ->
                val mut = ConfigRoot(this.map, this::set)
                for ((k, v) in map) {
                    mut.get(k).merge(v)
                }
            },
            { list -> get("").merge(list) },
            { plain -> if (plain != null) get("").merge(plain) }
        )
    }

    override val list: List<Any?> get() = get("").list
    override val plain: Any? get() = get("").plain

    private val mutableMap: LinkedHashMap<String, Any> get() =
        map as? LinkedHashMap<String, Any> ?: LinkedHashMap(map).also(this::set)
}

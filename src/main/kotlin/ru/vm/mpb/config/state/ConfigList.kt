package ru.vm.mpb.config.state

class ConfigList(
    override val list: List<Any?>,
    mutator: ConfigMutator
): Config(mutator) {

    override val value: List<Any?> = list

    override fun get(key: String) = toConfig(null) {
        set(mutableMapOf("" to list, key to it))
    }

    override fun get(index: Int) = toConfig(list.getOrNull(index)) {
        putNonNull(mutateList(list), index to it)
    }

    override fun add(value: Any) {
        mutateList(list).add(value)
    }

    override fun merge(value: Any) {
        mapValueByType(value,
            { map -> ConfigMap(map.toMutableMap(), this::set).also { set(it) }.merge(list) },
            { list ->
                ConfigRoot(this.list, this::set).also { mut ->
                    list.withIndex().filter { it.value != null }.forEach { mut.get(it.index).merge(it.value!!) }
                }
            },
            { plain -> set(plain!!) }
        )
    }

    override val plain: Any? get() = list.firstOrNull()
    override val map: Map<String, Any> get() = mapOf("" to list)

    private fun <T> mutateList(list: List<T>): ArrayList<T> =
        list as? ArrayList<T> ?: ArrayList(list).also(this::set)

}
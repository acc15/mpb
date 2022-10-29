package ru.vm.mpb.config.state

class ConfigList(
    override val list: List<Any?>,
    mutator: ConfigMutator
): Config(mutator) {

    override val value: List<Any?> = list

    override fun get(key: String) = of(null) {
        set(applyValues(LinkedHashMap(), "" to list, key to it))
    }

    override fun get(index: Int) = of(list.getOrNull(index)) {
        applyValues(mutateList(), index to it)
    }

    override fun add(value: Any?) { mutateList().add(value) }

    override fun merge(value: Any?) {
        mapValueByType(value,
            { map ->
                val newMap = LinkedHashMap(map)
                set(newMap)
                ConfigMap(newMap, this::set).merge(list)
            },
            { list ->
                val mut = getConfigForMerge()
                for ((i, v) in list.withIndex()) {
                    mut.get(i).merge(v)
                }
            },
            { plain -> set(plain) },
            { }
        )
    }

    override val plain: Any? get() = get(0).plain
    override val map: Map<String, Any> get() = mapOf("" to list)

    private fun getConfigForMerge(): Config {
        val plainList = isPlainList(list)

        if (list is ArrayList<*>) {
            if (plainList) {
                list.clear()
            }
            return this
        }

        if (plainList && list.isNotEmpty()) {
            val newList = ArrayList<Any?>()
            set(newList)
            return ConfigList(newList, this::set)
        }

        return ConfigRoot(list, this::set)
    }

    private fun mutateList(): ArrayList<Any?> =
        list as? ArrayList<Any?> ?: ArrayList(list).also(this::set)

}
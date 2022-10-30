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
        applyValues(mutableList, index to it)
    }

    override fun add(other: Any?) {
        mutableList.add(other)
    }

    override fun merge(other: Any?) {
        mapByType(other,
            { map -> ConfigMap(LinkedHashMap(map).also(this::set), this::set).merge(list) },
            { list ->
                val mut = getConfigForMerge()
                for ((i, v) in list.withIndex()) {
                    mut.get(i).merge(v)
                }
            },
            { plain ->
                if (plain != null) {
                    if (isPlainList(list)) {
                        set(plain)
                    } else {
                        get(0).merge(plain)
                    }
                }
            }
        )
    }

    override val plain: Any? get() = get(0).plain
    override val map: Map<String, Any> get() = mapOf("" to list)

    private fun getConfigForMerge(): Config {
        val isPlainList = isPlainList(list)
        if (list is ArrayList<*>) {
            if (isPlainList) {
                list.clear()
            }
            return this
        }

        if (isPlainList && list.isNotEmpty()) {
            return ConfigList(ArrayList<Any?>().also(this::set), this::set)
        }

        return ConfigRoot(list, this::set)
    }

    private val mutableList: ArrayList<Any?> get() = list as? ArrayList<Any?> ?: ArrayList(list).also(this::set)

}
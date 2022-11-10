package ru.vm.mpb.config.state

class ConfigPlain(
    override val value: Any?,
    mutator: ConfigMutator
): AbstractConfig(mutator) {

    override fun get(key: String) = Config.of(null) {
        if (it != null) set(Config.applyValues(LinkedHashMap(), "" to value, key to it))
    }

    override fun get(index: Int) = Config.of(null) {
        if (it != null) set(Config.applyValues(ArrayList(), 0 to value, index to it))
    }

    override fun add(other: Any?) {
        if (other == null) {
            return
        }
        if (value == null) {
            set(other)
            return
        }
        val list = ArrayList<Any?>()
        list.add(value)
        list.add(other)
        set(list)
    }

    override fun merge(other: Any?) {
        Config.mapByType(other,
            { map -> ConfigMap(LinkedHashMap<String, Any>().also(this::set), this::set).merge(map) },
            { list -> ConfigList(ArrayList<Any?>().also(this::set), this::set).merge(list) },
            { plain -> if (plain != null) set(plain) })
    }

    override val list: List<Any?> get() = if (value != null) listOf(value) else emptyList()
    override val map: Map<String, Any> get() = if (value != null) mapOf("" to value) else emptyMap()
    override val plain: Any? get() = value

}
package ru.vm.mpb.config.state

class ConfigShorthand(
    val main: Config,
    val alt: Config
): Config {

    override fun get(key: String) = ConfigShorthand(main.get(key), alt)
    override fun get(index: Int) = ConfigShorthand(main.get(index), alt)

    override fun add(other: Any?) = main.add(other)
    override fun merge(other: Any?) = main.merge(other)
    override fun set(other: Any?) = main.set(other)

    override val value: Any? get() = main.value
    override val list: List<Any?> get() = main.list
    override val map: Map<String, Any> get() = main.map
    override val plain: Any? get() = main.plain

    override val shorthand by lazy { ConfigComposite(alt, main) }
}
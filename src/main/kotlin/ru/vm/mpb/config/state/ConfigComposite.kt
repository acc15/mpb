package ru.vm.mpb.config.state

class ConfigComposite(val configs: Iterable<Config>): Config {
    constructor(vararg configs: Config): this(configs.toList())

    override fun get(key: String) = ConfigComposite(configs.map { it.get(key) })
    override fun get(index: Int) = ConfigComposite(configs.map { it.get(index) })

    override fun add(other: Any?) = configs.forEach { it.add(other) }
    override fun merge(other: Any?) = configs.forEach { it.merge(other) }
    override fun set(other: Any?) = configs.forEach { it.set(other) }

    override val value: Any? get() = configs.firstNotNullOfOrNull { it.value }
    override val plain: Any? get() = configs.firstNotNullOfOrNull { it.plain }
    override val list: List<Any?> get() = Config.mergeAll(configs).list
    override val map: Map<String, Any> get() = Config.mergeAll(configs).map
}
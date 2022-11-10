package ru.vm.mpb.config.state

class ConfigShorthand(val main: Config, val alt: Config): Config by main {
    override fun get(key: String) = ConfigShorthand(main.get(key), alt)
    override fun get(index: Int) = ConfigShorthand(main.get(index), alt)
    override val shorthand by lazy { ConfigComposite(alt, main) }
}
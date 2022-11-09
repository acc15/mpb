package ru.vm.mpb.config.loader

import org.fusesource.jansi.Ansi
import org.fusesource.jansi.AnsiConsole
import ru.vm.mpb.config.MpbPath
import ru.vm.mpb.config.state.Config
import ru.vm.mpb.config.state.ConfigRoot
import ru.vm.mpb.util.OrderedHashMap
import java.nio.file.Path
import java.util.*

object ConfigLoader {

    private val cleanupKeys = setOf("profiles", "profile", "config")

    fun configPaths(cfg: Config, base: Path? = null, defaultFn: (() -> List<Path>)? = null): List<Path> {
        val list = cfg.get("config").paths
        val result = if (list.isEmpty() && defaultFn != null) defaultFn() else list
        return if (base == null) result else result.map { base.resolve(it) }
    }

    fun load(vararg args: String): Config {
        val argCfg = Config.parseArgs(*args)

        val configPaths = configPaths(argCfg) { listOf(MpbPath.home.resolve("mpb.yaml")) }
        val activeProfiles = argCfg.get("profile").stringSet

        val cfg = ConfigRoot()
        mergeConfigs(configPaths, activeProfiles, cfg)
        cfg.merge(argCfg.value)

        return cleanup(cfg)
    }

    data class MergeItem(
        val path: Path,
        val value: Any,
        val profile: Boolean,
        var processed: Boolean = false
    )

    fun mergeConfigs(paths: List<Path>, activeProfiles: Iterable<String>, dest: Config) {

        val loadCache = mutableMapOf<Path, Any>()

        fun loadPaths(paths: List<Path>, profile: Boolean): List<MergeItem> {
            return paths.map { it to loadCache.computeIfAbsent(it) { p -> YamlLoader.loadOrNull(p) ?: Unit } }
                .filter { it.second != Unit }
                .map { MergeItem(it.first, it.second, profile) }
        }

        val process = LinkedList(loadPaths(paths, false))
        val stack = OrderedHashMap<Path, Unit>()
        while (process.isNotEmpty()) {

            val item = process.peek()

            val cfg = Config.ofImmutable(item.value)
            if (!item.processed) {
                item.processed = true
                if (checkHasCycle(item.path, stack)) {
                    process.remove()
                    continue
                }

                val parent = loadPaths(configPaths(cfg, item.path.parent), item.profile)
                if (parent.isNotEmpty()) {
                    process.addAll(0, parent)
                    continue
                }

            }

            process.remove()
            stack.remove(item.path)

            dest.merge(item.value)

            process.addAll(if (item.profile) 0 else process.size, activeProfiles
                .mapNotNull { cfg.get("profiles").get(it).value }
                .map { MergeItem(item.path, it, true) })
        }

    }

    private fun cleanup(cfg: Config): Config {
        for (k in cleanupKeys) {
            cfg.get(k).remove()
        }
        return cfg
    }

    private fun checkHasCycle(path: Path, stack: OrderedHashMap<Path, Unit>): Boolean {
        if (stack.put(path, Unit) == null) {
            return false
        }

        val cycle = (stack.subListOf(path).map { it.key } + path).joinToString(" -> ")
        AnsiConsole.err().println(
            Ansi.ansi().bold().fgRed().a("[error]").reset().a(" config cycle detected: $cycle")
        )
        return true
    }

}
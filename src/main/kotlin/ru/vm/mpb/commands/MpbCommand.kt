package ru.vm.mpb.commands

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.decodeFromStream
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.findOrSetObject
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.path
import java.nio.file.Path
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import kotlin.io.path.inputStream

class MpbCommand : CliktCommand() {

    /** Config file location */
    val configPath: Path by option("-c", "--config", help = "Config file location")
        .path()
        .default(Path.of("mpb.yaml"))

    /** Project options */
    val project by ProjectOptions()

    val config by findOrSetObject {
        val yaml = Yaml(configuration = YamlConfiguration(allowAnchorsAndAliases = true))
        configPath.inputStream().use { yaml.decodeFromStream(ru.vm.mpb.configuration.MpbConfig.serializer(), it) }
    }

    override fun aliases(): Map<String, List<String>> = mapOf(
        "b" to listOf("build"),
        "p" to listOf("pull"),
        "c" to listOf("switch"),
        "s" to listOf("switch")
    )

    override fun run() {

        println("config loaded: $config")

    }
}

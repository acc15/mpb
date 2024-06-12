package ru.vm.mpb.config

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import java.nio.file.Path




/*




class ConfigSerializationTest {

    val serializer = Yaml(configuration = YamlConfiguration(strictMode = false, allowAnchorsAndAliases = true, ))

    @Test
    fun testDefault() {
        val parsedConfig = serializer.decodeFromString(NewMpbConfig.serializer(), """
                name: abc
            """.trimIndent())
        assertEquals(NewMpbConfig("abc"), parsedConfig)
    }

    @Test
    fun testOutput() {
        val parsedConfig = serializer.decodeFromString(NewMpbConfig.serializer(), """
                name: abc
                output: { plain: true, monochrome: true, width: 120 }
            """.trimIndent())
        assertEquals(NewMpbConfig("abc", output = OutputConfig(plain = true, monochrome = true, 120)), parsedConfig)
    }

}*/
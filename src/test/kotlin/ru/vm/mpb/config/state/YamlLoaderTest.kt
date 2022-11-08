package ru.vm.mpb.config.state

import kotlin.test.*

import org.junit.jupiter.api.Assertions.*
import ru.vm.mpb.config.MpbConfigTest
import ru.vm.mpb.config.testConfig
import java.io.InputStreamReader
import java.io.StringReader

class YamlLoaderTest {

    @Test
    fun loadYaml() {
        val yaml = InputStreamReader(MpbConfigTest::class.java.getResourceAsStream("config.mpb.yaml")!!).use {
            YamlLoader.load(it)
        }
        assertEquals(testConfig, yaml)
    }

    @Test
    fun loadArrayYaml() {
        val value = YamlLoader.load(StringReader("[a,b,c]"))
        assertEquals(listOf("a", "b", "c"), value)
    }

    @Test
    fun loadObjectYaml() {
        val value = YamlLoader.load(StringReader("{a: b}"))
        assertEquals(mapOf("a" to "b"), value)
    }

    @Test
    fun loadEmptyYaml() {
        val state = YamlLoader.load(StringReader(""))
        assertNull(state)
    }

    @Test
    fun loadMultiYaml() {
        val state = YamlLoader.load(StringReader("a: b\n###\nc: d"))
        assertEquals(mapOf("a" to "b", "c" to "d"), state)
    }

}
package ru.vm.mpb.config

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ConfigMapTest {

    private val testArgs = listOf(
        "pull",
        "--config", "abc",
        "--projects.d.deps", "a", "b",
        "--args.a", "a", "b"
    )

    @Test
    fun parseKeys() {
        assertEquals(emptyList(), ConfigMap.parseKeys(""))
        assertEquals(listOf("a", "b", "c"), ConfigMap.parseKeys("a.b.c"))
    }

    @Test
    fun canParseArgs() {
        assertEquals(
            mapOf(
                "args" to mapOf("" to listOf("pull"), "a" to listOf("a", "b")),
                "config" to listOf("abc"),
                "projects" to mapOf(
                    "d" to mapOf(
                        "deps" to listOf("a", "b")
                    )
                )
            ),
            ConfigMap.parseArgs(testArgs).map
        )
    }

    @Test
    fun nonOptMappedAsArgs() {
        assertEquals(
            mapOf("args" to listOf("non", "opt")),
            ConfigMap.parseArgs(listOf("non", "opt")).map
        )
    }

    @Test
    fun canParseFlagOption() {
        assertEquals(
            mapOf(
                "debug" to emptyList<String>()
            ),
            ConfigMap.parseArgs(listOf("--debug")).map
        )
    }

    @Test
    fun canParseOptionList() {
        assertEquals(
            mapOf("args" to listOf("a", "b")),
            ConfigMap.parseArgs(listOf("--args", "a", "b")).map,
        )
    }

    @Test
    fun canConvertListToMap() {
        assertEquals(
            mapOf("args" to mapOf(
                "" to listOf("a", "b"),
                "fb" to listOf("x", "y")
            )),
            ConfigMap.parseArgs(listOf(
                "--args", "a", "b",
                "--args.fb", "x", "y"
            )).map
        )
    }

    @Test
    fun canAddListToMap() {
        assertEquals(
            mapOf("args" to mapOf(
                "" to listOf("a", "b"),
                "fb" to listOf("x", "y")
            )),
            ConfigMap.parseArgs(listOf(
                "--args.fb", "x", "y",
                "--args", "a", "b"
            )).map
        )
    }

    @Test
    fun canParseConfig() {
        ConfigMap.loadYaml(ConfigMapTest::class.java.getResourceAsStream("test-config.yaml")!!)
    }

    @Test
    fun canMergeConfigMaps() {
        val m1 = ConfigMap.loadYaml(ConfigMapTest::class.java.getResourceAsStream("test-config.yaml")!!)
        val m2 = ConfigMap.parseArgs(testArgs)

        val mm = m1.merge(m2)
        println(mm)
    }


}
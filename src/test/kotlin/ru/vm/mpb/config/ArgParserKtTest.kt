package ru.vm.mpb.config

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class ArgParserKtTest {

    private val testArgs = listOf(
        "pull",
        "--config", "abc",
        "--projects.d.deps", "a", "b",
        "--args.a", "a", "b"
    )

    @Test
    fun parseKeys() {
        assertEquals(emptyList(), parseKeys("--"))
        assertEquals(listOf("a", "b", "c"), parseKeys("--a.b.c"))
    }

    @Test
    fun canParseArgs() {
        assertEquals(
            mapOf(
                "debug" to emptyList<String>(),
                "args" to mapOf(
                    "" to listOf("pull"),
                    "fb" to listOf("a", "b")
                ),
                "config" to listOf("abc"),
                "projects" to mapOf(
                    "fb" to mapOf(
                        "deps" to listOf("sf", "ff")
                    )
                )
            ),
            parseArgs(testArgs)
        )
    }

    @Test
    fun nonOptMappedAsArgs() {
        assertEquals(
            mapOf("args" to mapOf("" to listOf("non", "opt"))),
            parseArgs(listOf("non", "opt"))
        )
    }

    @Test
    fun canParseFlagOption() {
        assertEquals(
            mapOf(
                "debug" to emptyList<String>(),
                "args" to mapOf("" to emptyList<String>())),
            parseArgs(listOf("--debug"))
        )
    }

    @Test
    fun canParseOptionList() {
        assertEquals(
            mapOf("args" to mapOf("" to listOf("a", "b"))),
            parseArgs(listOf("--args", "a", "b")),
        )
    }

    @Test
    fun canConvertListToMap() {
        assertEquals(
            mapOf("args" to mapOf(
                "" to listOf("a", "b"),
                "fb" to listOf("x", "y")
            )),
            parseArgs(listOf(
                "--args", "a", "b",
                "--args.fb", "x", "y"
            ))
        )
    }

    @Test
    fun canAddListToMap() {
        assertEquals(
            mapOf("args" to mapOf(
                "" to listOf("a", "b"),
                "fb" to listOf("x", "y")
            )),
            parseArgs(listOf(
                "--args.fb", "x", "y",
                "--args", "a", "b"
            ))
        )
    }

    @Test
    fun canParseConfig() {
        val cfg = loadConfig(ArgParserKtTest::class.java.getResourceAsStream("test-config.yaml")!!)
        assertIs<Map<String, Any>>(cfg)
    }

    @Test
    fun canMergeConfigMaps() {
        val m1 = loadConfig(ArgParserKtTest::class.java.getResourceAsStream("test-config.yaml")!!)
        val m2 = parseArgs(testArgs)

        val mm = mergeConfigMaps(listOf(m1, m2))
        println(mm)
    }


}
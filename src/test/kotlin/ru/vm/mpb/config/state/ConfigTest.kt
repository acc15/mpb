package ru.vm.mpb.config.state

import org.junit.jupiter.api.DynamicTest
import kotlin.test.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import ru.vm.mpb.config.MpbConfigTest
import ru.vm.mpb.config.testConfig
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

class ConfigTest {

    private val testArgs = arrayOf(
        "pull",
        "--config", "abc",
        "--projects.d.deps", "a", "b",
        "--args.a", "a", "b"
    )

    private val EXPECTED_MAP = mapOf(
        "a" to mapOf("b" to "HAHA"),
        "b" to listOf("VALUE0", "VALUE1"),
        "c" to "PLAIN",
        "d" to listOf("E1", "E2"),
        "e" to listOf(listOf("X", "Y")),
        "" to listOf(null, "A", null, "B", null, "C")
    )

    @Test
    fun putAllList() {
        assertEquals<List<Any?>>(
            listOf(null, "X", "Y", null, null, "Z"),
            Config.putNonNull(mutableListOf(), 1 to "X", 2 to "Y", 5 to "Z")
        )
    }

    @Test
    fun plainAdd() {
        val m = ConfigRoot()
        m.add("abc")
        assertEquals("abc", m.value)

        m.add("xyz")
        assertEquals(listOf("abc", "xyz"), m.value)
    }

    @Test
    fun plainGetMustReturnCorrectValue() {
        val v = ConfigPlain(10) {}
        assertEquals(null, v.get(0).value)
        assertEquals(null, v.get(1).value)
        assertEquals(null, v.get("").value)
        assertEquals(null, v.get("x").value)
    }

    @Test
    fun listGetMustReturnCorrectValue() {
        val v = ConfigList(listOf(1, 2, 3)) {}
        assertEquals(1, v.get(0).value)
        assertEquals(null, v.get(3).value)
        assertEquals(null, v.get("").value)
    }

    @Test
    fun mapGetMustReturnCorrectValue() {
        val v = ConfigMap(mapOf("x" to 1, "" to 5)) {}
        assertEquals(1, v.get("x").value)
        assertEquals(null, v.get("y").value)
        assertEquals(5, v.get("").value)
        assertEquals(null, v.get("").get(0).value)
    }

    @Test
    fun getKeepsCollectionsImmutable() {
        val map: MutableMap<String, Any> = mutableMapOf(
            "a" to Collections.unmodifiableList(listOf("x")),
            "b" to Collections.unmodifiableMap(mapOf("x" to "y"))
        )
        val m = ConfigRoot(map)
        assertFalse(map["a"] is ArrayList<*>)
        assertFalse(map["b"] is LinkedHashMap<*, *>)

        m.get("a")
        assertFalse(map["a"] is ArrayList<*>)

        m.get("b")
        assertFalse(map["b"] is LinkedHashMap<*, *>)
    }

    @Test
    fun addMakesCollectionsMutable() {
        val map: MutableMap<String, Any> = mutableMapOf(
            "a" to Collections.unmodifiableList(listOf("x")),
            "b" to Collections.unmodifiableMap(mapOf("x" to "y"))
        )

        val m = ConfigRoot(map)
        assertFalse(map["a"] is ArrayList<*>)
        assertFalse(map["b"] is LinkedHashMap<*, *>)

        m.get("a").add("y")
        assertTrue(map["a"] is ArrayList<*>)
        assertFalse(map["b"] is LinkedHashMap<*, *>)

        m.get("b").get("y").add("x")
        assertTrue(map["b"] is LinkedHashMap<*, *>)
    }

    @Test
    fun getAdd() {
        val m = ConfigRoot()
        m.get("a").get("b").add("HAHA")
        m.get("b").get(0).add("VALUE0")
        m.get("b").get(1).add("VALUE1")
        m.get("c").add("PLAIN")
        m.get("d").add("E1")
        m.get("d").add("E2")
        m.get("e").get(0).get(0).add("X")
        m.get("e").get(0).get(1).add("Y")
        m.get(1).add("A")
        m.get(3).add("B")
        m.get(5).add("C")
        assertEquals(EXPECTED_MAP, m.value)
    }

    @Test
    fun pathAdd() {
        val m = ConfigRoot()
        m.path("a.b").add("HAHA")
        m.path("b[0]").add("VALUE0")
        m.path("b[1]").add("VALUE1")
        m.path("c").add("PLAIN")
        m.path("d").add("E1")
        m.path("d").add("E2")
        m.path("e[0][0]").add("X")
        m.path("e[0][1]").add("Y")
        m.path("[1]").add("A")
        m.path("[3]").add("B")
        m.path("[5]").add("C")
        assertEquals(EXPECTED_MAP, m.value)
    }

    @Test
    fun set() {
        val m = ConfigRoot()
        m.add("X")
        m.add("Y")
        m.set("Z")
        assertEquals(m.value, "Z")
    }

    @Test
    fun mergeNullToList() {
        val m1 = ConfigRoot(emptyMap<String, Any>())
        val m2 = ConfigRoot(mapOf("args" to listOf("c", "417")))
        m1.merge(m2.value)
        assertEquals(mapOf("args" to listOf("c", "417")), m1.value)
    }

    @Test
    fun merge() {

        val m1 = ConfigRoot()
        m1.path("list[0].p1").set("p1")
        m1.path("list[0].p2").set("p2")
        m1.path("args.sf").add("args.sf.1")
        m1.path("args.sf").add("args.sf.2")
        m1.path("debug").set(false)

        val m2 = ConfigRoot()
        m2.path("list[0].p1").set("override")
        m2.path("args").add("d")
        m2.path("debug").set(true)

        m1.merge(m2.value)

        assertEquals(
            mapOf(
                "list" to listOf(
                    mapOf(
                        "p1" to "override",
                        "p2" to "p2"
                    )
                ),
                "args" to mapOf(
                    "sf" to listOf("args.sf.1", "args.sf.2"),
                    "" to "d"
                ),
                "debug" to true
            ),
            m1.value
        )

    }

    @TestFactory
    fun parsePath() = mapOf(
        "" to emptyList(),
        "a.b[0][1].c.d" to listOf("a", "b", 0, 1, "c", "d"),
        ".abc" to listOf("", "abc"),
        "[].abc" to listOf("", "abc"),
        "..." to listOf("", "", ""),
        "[ 15 ]" to listOf(15),
        "15" to listOf("15"),
        "abc[xyz]" to listOf("abc", "xyz"),
        "[abc][xyz]" to listOf("abc", "xyz"),
        "[[abc]]" to listOf("[abc]"),
        "[abc].[xyz]" to listOf("abc", "", "xyz"),
    ).map {
        DynamicTest.dynamicTest("parseConfigPath: ${it.key}") {
            val actualSegments = Config.parsePath(it.key)
            val expectedSegments = it.value
            assertEquals(expectedSegments, actualSegments)
        }
    }

    @TestFactory
    fun parseArgs() = listOf(
        testArgs to mapOf(
            "args" to mapOf("" to "pull", "a" to listOf("a", "b")),
            "config" to "abc",
            "projects" to mapOf(
                "d" to mapOf(
                    "deps" to listOf("a", "b")
                )
            )
        ),
        arrayOf("non", "opt") to mapOf("args" to listOf("non", "opt")),
        arrayOf("--debug") to mapOf("debug" to true),
        arrayOf("--args", "a", "b") to mapOf("args" to listOf("a", "b")),
        arrayOf("--args", "a", "b", "--args.fb", "x", "y") to mapOf(
            "args" to mapOf(
                "" to listOf("a", "b"),
                "fb" to listOf("x", "y")
            )
        ),
        arrayOf("--indexed[0].value", "a", "--indexed[2].value", "b") to mapOf(
            "indexed" to listOf(
                mapOf("value" to "a"),
                null,
                mapOf("value" to "b")
            )
        ),
    ).map {
        DynamicTest.dynamicTest("parseConfigArgs: ${it.first}") {
            val expected = it.second
            val actual = Config.parseArgs(it.first).value
            assertEquals(expected, actual)
        }
    }

    @Test
    fun parseYaml() {
        val state = Config.parseYaml(MpbConfigTest::class.java.getResource("config.mpb.yaml")!!)
        assertEquals(testConfig, state.value)
    }

    @Test
    fun mergeAll() {

        val m1 = ConfigRoot(mutableMapOf("a" to 1))
        val m2 = ConfigRoot(mutableMapOf("b" to 2))

        val m = Config.mergeAll(listOf(m1, m2).reversed())

        assertEquals(m.value, mapOf("a" to 1, "b" to 2))
        assertEquals(m1.value, mapOf("a" to 1))
        assertEquals(m2.value, mapOf("b" to 2))

    }
}
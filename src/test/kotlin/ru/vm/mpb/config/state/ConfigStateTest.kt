package ru.vm.mpb.config.state

import kotlin.test.*
import org.junit.jupiter.api.Test
import ru.vm.mpb.config.MpbConfigKtTest
import ru.vm.mpb.config.testConfig

internal class ConfigStateTest {

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
            putNonNull(mutableListOf(), 1 to "X", 2 to "Y", 5 to "Z")
        )
    }

    @Test
    fun plainAdd() {
        val m = ConfigMutableState()
        m.add("abc")
        assertEquals("abc", m.value)

        m.add("xyz")
        assertEquals(listOf("abc", "xyz"), m.value)
    }

    @Test
    fun getMustChangeImmutableCollectionsToMutable() {
        val map: MutableMap<String, Any> = mutableMapOf(
            "a" to listOf("x"),
            "b" to mapOf("x" to "y")
        )
        val m = ConfigMutableMapState(map) {}

        m.get("a")
        assertTrue(map["a"] is ArrayList<*>)

        m.get("b")
        assertTrue(map["b"] is LinkedHashMap<*, *>)
    }

    @Test
    fun getAdd() {
        val m = ConfigMutableState()
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
        val m = ConfigMutableState()
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
        val m = ConfigMutableState()
        m.add("X")
        m.add("Y")
        m.set("Z")
        assertEquals(m.value, "Z")
    }

    @Test
    fun merge() {

        val m1 = ConfigMutableState()
        m1.path("list[0].p1").set("p1")
        m1.path("list[0].p2").set("p2")
        m1.path("args.sf").add("args.sf.1")
        m1.path("args.sf").add("args.sf.2")
        m1.path("debug").set(false)

        val m2 = ConfigMutableState()
        m2.path("list[0].p1").set("override")
        m2.path("args").add("d")
        m2.path("debug").set(true)

        m1.merge(m2.value!!)

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

    @Test
    fun canLoadConfigYaml() {
        val state = loadConfigYaml(MpbConfigKtTest::class.java.getResource("test-config.yaml")!!)
        assertEquals(testConfig, state.map)
    }

}
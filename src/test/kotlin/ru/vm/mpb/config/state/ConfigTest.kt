package ru.vm.mpb.config.state

import kotlin.test.*

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

val testArgs = arrayOf(
    "pull",
    "--config", "abc",
    "--projects.d.deps", "a", "b",
    "--args.a", "a", "b"
)

class ConfigTest {

    private val expectedMap = mapOf(
        "a" to mapOf("b" to "HAHA"),
        "b" to listOf("VALUE0", "VALUE1"),
        "c" to "PLAIN",
        "d" to listOf("E1", "E2"),
        "e" to listOf(listOf("X", "Y")),
        "" to listOf(null, "A", null, "B", null, "C")
    )

    @Test
    fun applyValues() {
        assertEquals<List<Any?>>(
            listOf(null, "X", "Y", null, null, "Z"),
            Config.applyValues(mutableListOf(), 1 to "X", 2 to "Y", 5 to "Z")
        )
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
        assertEquals(expectedMap, m.value)
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
        assertEquals(expectedMap, m.value)
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

    @Test
    fun mergeAll() {
        val m1 = Config.immutable(mutableMapOf("a" to 1))
        val m2 = Config.immutable(mutableMapOf("b" to 2))

        val m = Config.mergeAll(m1, m2)

        assertEquals(m.value, mapOf("a" to 1, "b" to 2))
        assertEquals(m1.value, mapOf("a" to 1))
        assertEquals(m2.value, mapOf("b" to 2))
    }

    @Test
    fun mergeMustAvoidOtherMutations() {
        val target = ConfigRoot()
        val other = mutableMapOf("b" to mutableListOf(1, 2, 3))

        target.merge(other)
        target.get("b").get(1).set(4)

        assertEquals(other, mapOf("b" to listOf(1, 2, 3)))
        assertEquals(target.value, mapOf("b" to listOf(1, 4, 3)))
    }

    @Test
    fun mergeMustDeeplyMergeListOfNonPlains() {
        val target = Config.immutable(mutableListOf(
            mutableMapOf("a" to "b"),
            mutableMapOf("b" to "c"),
            mutableMapOf("c" to "d")
        ))

        target.merge(mutableListOf(
            mutableMapOf("c" to "d", "d" to "e"),
            mutableMapOf("b" to "f", "f" to "g")
        ))

        assertEquals(target.value, listOf(
            mapOf("a" to "b", "c" to "d", "d" to "e"),
            mapOf("b" to "f", "f" to "g"),
            mapOf("c" to "d")
        ))
    }

    @Test
    fun mergeMustReplaceListOfPlains() {
        val target = ConfigRoot(mutableMapOf("list" to mutableListOf("very", "long", "list")))
        target.merge(mutableMapOf("list" to mutableListOf("short", "list")))
        assertEquals(mapOf("list" to listOf("short", "list")), target.value)
    }

    @Test
    fun shorthand() {

        val root = Config.immutable(mapOf(
            "v1" to "v1",
            "deep" to mapOf(
                "list" to listOf(
                    mapOf(
                        "v1" to "deep.list[0].v1",
                        "v2" to "deep.list[0].v2"
                    )
                ),
                "object" to mapOf(
                    "v1" to "deep.object.v1",
                    "v2" to "deep.object.v2"
                )
            )
        )).shorthand()

        val map = root.get("deep").configMap
        val obj = map.getValue("object")
        assertEquals("deep.object.v1", obj.get("v1").string)
        assertEquals("deep.object.v2", obj.get("v2").string)
        assertEquals("v1", obj.shorthand.get("v1").string)
        assertEquals("deep.object.v2", obj.shorthand.get("v2").string)

        val list = map.getValue("list").configList[0]
        assertEquals("deep.list[0].v1", list.get("v1").string)
        assertEquals("deep.list[0].v2", list.get("v2").string)
        assertEquals("v1", list.shorthand.get("v1").string)
        assertEquals("deep.list[0].v2", list.shorthand.get("v2").string)

    }
}
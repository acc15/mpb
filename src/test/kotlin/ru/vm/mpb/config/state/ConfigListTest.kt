package ru.vm.mpb.config.state

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import kotlin.test.*

class ConfigListTest {

    @TestFactory
    fun setByKey() = listOf<Pair<Pair<String, Any?>, Any?>>(
        ("" to "abc") to mapOf("" to "abc"),
        ("" to null) to emptyMap<String, Any>()
    ).map {
        DynamicTest.dynamicTest("setByKey: ${it.first}") {
            val target = ConfigRoot(mutableListOf<Any?>(1, 2, 3))
            target.get(it.first.first).set(it.first.second)
            assertEquals(it.second, target.value)
        }
    }

    @TestFactory
    fun setByIndex() = listOf<Pair<Pair<Int, Any?>, Any?>>(
        (1 to null) to listOf(1, null, 3),
        (10 to null) to listOf(1, 2, 3)
    ).map {
        DynamicTest.dynamicTest("setByKey: ${it.first}") {
            val target = ConfigRoot(mutableListOf<Any?>(1, 2, 3))
            target.get(it.first.first).set(it.first.second)
            assertEquals(it.second, target.value)
        }
    }

    @Test
    fun getMustReturnCorrectValue() {
        val v = ConfigList(listOf(1, 2, 3), immutable)
        assertEquals(1, v.get(0).value)
        assertNull(v.get(3).value)
        assertEquals(listOf(1, 2, 3), v.get("").value)
    }

    @TestFactory
    fun plainMerge() = listOf<Pair<Any?, Any?>>(
        null to listOf(1, 2, 3),
        10 to 10,
        "abc" to "abc",
        listOf("x", "y") to listOf("x", "y")
    ).map {
        DynamicTest.dynamicTest("plainListMerge: ${it.first}") {
            val target = ConfigRoot(listOf(1, 2, 3))
            target.merge(it.first)
            assertEquals(it.second, target.value)
        }
    }

    @TestFactory
    fun complexMerge() = listOf(
        null to listOf(mapOf("a" to "b"), mapOf("c" to "d")),
        10 to listOf(mapOf("a" to "b", "" to 10), mapOf("c" to "d")),
        listOf(listOf("x", "y"), mapOf("e" to "f")) to listOf(
            mapOf("a" to "b", "" to listOf("x", "y")),
            mapOf("c" to "d", "e" to "f")
        )
    ).map {
        DynamicTest.dynamicTest("complexMerge: ${it.first}") {
            val target = ConfigRoot(listOf(mapOf("a" to "b"), mapOf("c" to "d")))
            target.merge(it.first)
            assertEquals(it.second, target.value)
        }
    }

    @Test
    fun list() {
        assertEquals(emptyList(), ConfigList(emptyList(), immutable).list)
        assertEquals(listOf(10), ConfigList(listOf(10), immutable).list)
    }

    @Test
    fun map() {
        assertEquals(mapOf("" to listOf(10)), ConfigList(listOf(10), immutable).map)
        assertEquals(emptyMap(), ConfigList(emptyList(), immutable).map)
    }

    @Test
    fun plain() {
        assertEquals(10, ConfigList(listOf(10), immutable).plain)
        assertEquals(null, ConfigList(emptyList(), immutable).plain)
    }

    @Test
    fun configList() {
        assertEquals(10, ConfigList(listOf(10), immutable).configList[0].value)
        assertTrue(ConfigList(emptyList(), immutable).configList.isEmpty())
    }

    @Test
    fun configMap() {
        assertEquals(listOf(10), ConfigList(listOf(10), immutable).configMap.getValue("").value)
        assertTrue(ConfigList(emptyList(), immutable).configMap.isEmpty())
    }

}
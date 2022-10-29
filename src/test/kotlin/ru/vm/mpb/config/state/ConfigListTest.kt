package ru.vm.mpb.config.state

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConfigListTest {

    @Test
    fun setValueByDefaultKeyMustMutateToMap() {
        val target = ConfigRoot(mutableListOf<Any?>(1, 2, 3))
        target.get("").set("abc")
        assertEquals(mapOf("" to "abc"), target.value)
    }

    @Test
    fun setNullByDefaultKeyMustMutateToEmptyMap() {
        val target = ConfigRoot(mutableListOf<Any?>(1, 2, 3))
        target.get("").set(null)
        assertEquals(emptyMap<String, Any>(), target.value)
    }

    @Test
    fun setNullByIndexMustSetNullValue() {
        val target = ConfigRoot(mutableListOf<Any?>(1, 2, 3))
        target.get(1).set(null)
        assertEquals(listOf(1, null, 3), target.value)
    }

    @Test
    fun setNullByIndexMustBeIgnoredWhenListIsShorten() {
        val target = ConfigRoot(mutableListOf<Any?>(1, 2, 3))
        target.get(10).set(null)
        assertEquals(listOf(1, 2, 3), target.value)
    }

}
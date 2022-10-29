package ru.vm.mpb.config.state

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConfigListTest {

    @Test
    fun setNullByDefaultKeyMustMutateToEmptyMap() {
        var value: Any? = mutableListOf<Any?>(1, 2, 3)
        val target = ConfigList(value as List<Any?>) { value = it}
        target.get("").set(null)
        assertEquals(emptyMap<String, Any>(), target.value)
    }

    @Test
    fun setNullByIndexMustSetNullValue() {
        val target = ConfigList(mutableListOf<Any?>(1, 2, 3)) {}
        target.get(1).set(null)
        assertEquals(listOf(1, null, 3), target.value)
    }

    @Test
    fun setNullByIndexMustBeIgnoredWhenListIsShorten() {
        val target = ConfigList(mutableListOf<Any?>(1, 2, 3)) {}
        target.get(10).set(null)
        assertEquals(listOf(1, 2, 3), target.value)
    }

}
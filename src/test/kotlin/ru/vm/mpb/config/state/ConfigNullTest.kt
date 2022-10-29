package ru.vm.mpb.config.state

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConfigNullTest {

    @Test
    fun setValueByKeyMustMutateToMap() {
        var value: Any? = null
        val n = ConfigNull { value = it }
        n.get("a").set(1)
        assertEquals(mapOf("a" to 1), value)
    }

    @Test
    fun setValueByIndexMustMutateToList() {
        var value: Any? = null
        val n = ConfigNull { value = it }
        n.get(3).set(1)
        assertEquals(listOf(null, null, null, 1), value)
    }

    @Test
    fun setNullByIndexMustKeepNullValue() {
        val n = ConfigNull(Config.immutable)
        n.get(10).set(null)
        assertEquals(null, n.value)
    }

    @Test
    fun setNullByKeyMustKeepNullValue() {
        val n = ConfigNull(Config.immutable)
        n.get("a").set(null)
        assertEquals(null, n.value)
    }

}
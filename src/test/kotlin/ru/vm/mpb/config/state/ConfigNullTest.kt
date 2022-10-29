package ru.vm.mpb.config.state

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConfigNullTest {

    @Test
    fun setValueByKeyMustMutateToMap() {
        val target = ConfigRoot(null)
        target.get("a").set(1)
        assertEquals(mapOf("a" to 1), target.value)
    }

    @Test
    fun setValueByIndexMustMutateToList() {
        val target = ConfigRoot(null)
        target.get(3).set(1)
        assertEquals(listOf(null, null, null, 1), target.value)
    }

    @Test
    fun setNullByIndexMustKeepNullValue() {
        val target = ConfigRoot(null)
        target.get(10).set(null)
        assertEquals(null, target.value)
    }

    @Test
    fun setNullByKeyMustKeepNullValue() {
        val target = ConfigRoot()
        target.get("a").set(null)
        assertEquals(null, target.value)
    }

}
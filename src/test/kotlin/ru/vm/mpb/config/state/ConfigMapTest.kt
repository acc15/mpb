package ru.vm.mpb.config.state

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConfigMapTest {

    @Test
    fun setNullByKeyMustRemoveEntry() {
        val target = ConfigRoot(mutableMapOf("a" to "b", "" to "abc"))
        target.get("").set(null)
        assertEquals(mapOf("a" to "b"), target.value)
    }

}
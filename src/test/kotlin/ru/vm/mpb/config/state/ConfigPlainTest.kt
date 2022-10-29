package ru.vm.mpb.config.state

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ConfigPlainTest {

    @Test
    fun setByIndexMutatesToList() {
        val target = ConfigRoot("a")
        target.get(2).set("y")
        assertEquals(listOf("a", null, "y"), target.value)
    }

    @Test
    fun setByKeyMutatesToMap() {
        val target = ConfigRoot("a")
        target.get("x").set("y")
        assertEquals(mapOf("" to "a", "x" to "y"), target.value)
    }

    @Test
    fun setNullByIndexDoesntChangeValue() {
        val target = ConfigRoot("a")
        target.get(1).set(null)
        assertEquals("a", target.value)
    }

    @Test
    fun setNullByKeyDoesntChangeValue() {
        val target = ConfigRoot("a")
        target.get("a").set(null)
        assertEquals("a", target.value)
    }

}
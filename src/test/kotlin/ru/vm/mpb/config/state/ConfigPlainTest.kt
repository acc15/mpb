package ru.vm.mpb.config.state

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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

    @Test
    fun setValueByKeyMustMutateToMap() {
        val target = ConfigRoot(null)
        target.get("a").set(1)
        Assertions.assertEquals(mapOf("a" to 1), target.value)
    }

    @Test
    fun setValueByIndexMustMutateToList() {
        val target = ConfigRoot(null)
        target.get(3).set(1)
        Assertions.assertEquals(listOf(null, null, null, 1), target.value)
    }

    @Test
    fun setNullByIndexMustKeepNullValue() {
        val target = ConfigRoot(null)
        target.get(10).set(null)
        Assertions.assertEquals(null, target.value)
    }

    @Test
    fun setNullByKeyMustKeepNullValue() {
        val target = ConfigRoot()
        target.get("a").set(null)
        Assertions.assertEquals(null, target.value)
    }

    @Test
    fun add() {
        val m = ConfigRoot()
        m.add("abc")
        assertEquals("abc", m.value)

        m.add("xyz")
        assertEquals(listOf("abc", "xyz"), m.value)
    }

    @Test
    fun getMustReturnCorrectValue() {
        val v = ConfigPlain(10) {}
        assertNull(v.get(0).value)
        assertNull(v.get(1).value)
        assertNull(v.get("").value)
        assertNull(v.get("x").value)
    }

}
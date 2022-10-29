package ru.vm.mpb.config.state

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ConfigPlainTest {

    @Test
    fun setByIndexMutatesToList() {
        var value: Any? = "a"
        val target = ConfigPlain("a") { value = it }
        target.get(2).set("y")
        assertEquals(listOf("a", null, "y"), value)
    }

    @Test
    fun setByKeyMutatesToMap() {
        var value: Any? = "a"
        val target = ConfigPlain("a") { value = it }
        target.get("x").set("y")
        assertEquals(mapOf("" to "a", "x" to "y"), value)
    }

    @Test
    fun setNullByIndexDoesntChangeValue() {
        var value: Any? = "a"
        val target = ConfigPlain("a") { value = it }
        target.get(1).set(null)
        assertEquals("a", value)
    }

    @Test
    fun setNullByKeyDoesntChangeValue() {
        val target = ConfigRoot("a")
        target.get("a").set(null)
        assertEquals("a", target.value)
    }

}
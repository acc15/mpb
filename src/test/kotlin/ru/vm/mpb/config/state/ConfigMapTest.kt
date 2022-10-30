package ru.vm.mpb.config.state

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.test.*

class ConfigMapTest {

    @Test
    fun setNullByKeyMustRemoveEntry() {
        val target = ConfigRoot(mutableMapOf("a" to "b", "" to "abc"))
        target.get("").set(null)
        assertEquals(mapOf("a" to "b"), target.value)
    }

    @Test
    fun getMustReturnCorrectValue() {
        val v = ConfigMap(mapOf("x" to 1, "" to 5)) {}
        assertEquals(1, v.get("x").value)
        assertNull(v.get("y").value)
        assertEquals(5, v.get("").value)
        assertNull(v.get("").get(0).value)
    }

    @Test
    fun getKeepsCollectionsImmutable() {
        val map: MutableMap<String, Any> = mutableMapOf(
            "a" to Collections.unmodifiableList(listOf("x")),
            "b" to Collections.unmodifiableMap(mapOf("x" to "y"))
        )
        val m = ConfigRoot(map)
        assertFalse(map["a"] is ArrayList<*>)
        assertFalse(map["b"] is LinkedHashMap<*, *>)

        m.get("a")
        assertFalse(map["a"] is ArrayList<*>)

        m.get("b")
        assertFalse(map["b"] is LinkedHashMap<*, *>)
    }


}
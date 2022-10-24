package ru.vm.mpb.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

data class TestData(
    val graph: Map<String, Set<String>>,
    val keys: Set<String>,
    val cycles: List<List<String>>,
    val dfsOrder: List<String>,
    val bfsOrder: List<String>
)

internal class GraphUtilsKtTest {

    private val testData = listOf(
        TestData(
            graph = mapOf(
                "a" to setOf("a")
            ),
            keys = setOf("a"),
            cycles = listOf(listOf("a", "a")),
            dfsOrder = listOf("a"),
            bfsOrder = emptyList()
        ),
        TestData(
            graph = mapOf(
                "a" to emptySet(),
                "b" to setOf("a"),
                "d" to emptySet()
            ),
            keys = setOf("b", "d"),
            cycles = emptyList(),
            dfsOrder = listOf("b", "a", "d"),
            bfsOrder = listOf("b", "d", "a")
        ),
        TestData(
            graph = mapOf(
                "a" to setOf("b"),
                "b" to setOf("a"),
                "c" to setOf("a")
            ),
            keys = setOf("a", "c"),
            cycles = listOf(listOf("a", "b", "a")),
            dfsOrder = listOf("a", "b", "c"),
            bfsOrder = emptyList()
        ),
        TestData(
            graph = mapOf(
                "a" to setOf("d"),
                "b" to setOf("a", "d"),
                "c" to setOf("b"),
                "d" to setOf("c")
            ),
            keys = setOf("a"),
            cycles = listOf(listOf("a", "d", "c", "b", "a"), listOf("d", "c", "b", "d")),
            dfsOrder = listOf("a", "d", "c", "b"),
            bfsOrder = emptyList()
        ),
        TestData(
            graph = mapOf(
                "a" to setOf("b"),
                "b" to setOf("c"),
                "c" to emptySet(),
                "x" to setOf("a"),
                "y" to setOf("b")
            ),
            keys = setOf("x", "y"),
            cycles = emptyList(),
            dfsOrder = listOf("x", "a", "b", "c", "y"),
            bfsOrder = listOf("x", "y", "a", "b", "c")
        ),
    )

    @TestFactory
    fun findCycles() = testData.map {
        DynamicTest.dynamicTest("findCycles: ${it.graph}") {
            val actual = mutableListOf<List<String>>()
            dfs(it.keys, it.graph::getValue, onCycle = actual::add)
            assertEquals(it.cycles, actual)
        }
    }

    @TestFactory
    fun dfsOrder() = testData.map {
        DynamicTest.dynamicTest("dfsOrder: ${it.graph}") {
            val actual = mutableListOf<String>()
            dfs(it.keys, it.graph::getValue, onNode = actual::add)
            assertEquals(it.dfsOrder, actual)
        }
    }

    @TestFactory
    fun bfsOrder() = testData.filter { it.bfsOrder.isNotEmpty() }.map {
        DynamicTest.dynamicTest("bfsOrder: ${it.graph}") {
            val actual = mutableListOf<String>()
            bfs(it.keys, it.graph::getValue, onNode = actual::add)
            assertEquals(it.bfsOrder, actual)
        }
    }

}
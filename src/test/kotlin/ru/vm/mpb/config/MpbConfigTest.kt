package ru.vm.mpb.config

import org.junit.jupiter.api.Test
import ru.vm.mpb.config.state.*
import kotlin.test.assertEquals

val testConfig get() = mapOf(
    "debug" to false,
    "baseDir" to "/baseDir",
    "branch" to mapOf(
        "default" to "master",
        "filters" to listOf(
            mapOf(
                "regex" to "^origin/(rc/3\\.\${branch}\\..*-sf)\$",
                "index" to "last"
            )
        )
    ),
    "build" to mapOf(
        "default" to mapOf(
            "env" to mapOf("ENV1" to "VALUE1"),
            "profiles" to mapOf(
                "default" to listOf("c", "d", "e"),
                "nt" to listOf("c", "d", "e", "F")
            )
        ),
        "java17" to mapOf(
            "env" to mapOf("JAVA_HOME" to "/usr/lib/jvm/java-17-openjdk"),
            "profiles" to mapOf(
                "default" to listOf("c", "d", "e"),
                "nt" to listOf("c", "d", "e", "F")
            )
        ),
        "yarn" to mapOf(
            "profiles" to mapOf("default" to listOf("a", "b"))
        )
    ),
    "projects" to mapOf(
        "a" to mapOf("dir" to "a"),
        "b" to mapOf(
            "dir" to "b",
            "deps" to listOf("a"),
            "build" to "java17"
        ),
        "c" to mapOf(
            "dir" to "c",
            "deps" to listOf("b"),
            "branch" to mapOf(
                "filters" to listOf(
                    mapOf(
                        "regex" to "base/test/*",
                        "index" to "first"
                    )
                )
            )
        ),
        "d" to mapOf("dir" to "d", "build" to "yarn")
    ),
    "jira" to mapOf(
        "url" to "https://jira/browse",
        "project" to "JIRA"
    ),
    "args" to mapOf(
        "d" to listOf("a", "b", "c"),
        "" to listOf("x", "y")
    ),
    "ticket" to mapOf("dir" to "ticket")
)


class MpbConfigTest {

    @Test
    fun parse() {

        val config = MpbConfig.parse(arrayOf(
            "pull",
            "--debug",
            "--", "a", "b",
            "--args.a", "args.a",
            "--args.b", "args.b",
            "--branch.filters[1].regex", "p2",
            "--branch.filters[1].index", "last"
        )) { ConfigRoot(testConfig) }

        assertEquals(true, config.debug)
        assertEquals("pull", config.command)
        assertEquals(listOf("a", "b"), config.commonArgs)
        assertEquals(listOf("args.a"), config.args["a"])
        assertEquals(listOf("args.b"), config.args["b"])
        assertEquals(BranchFilter("p2", -1), config.branch.filters[1])

    }

}
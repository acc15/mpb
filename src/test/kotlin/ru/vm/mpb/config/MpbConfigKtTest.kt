package ru.vm.mpb.config

import org.junit.jupiter.api.Test
import ru.vm.mpb.config.state.*
import kotlin.test.assertEquals

val testConfig get() = mapOf(
    "debug" to false,
    "defaultBranch" to "master",
    "baseDir" to "/baseDir",
    "branchPatterns" to listOf(
        mapOf(
            "pattern" to "origin/rc/3.\${input}*",
            "index" to "last"
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
            "branchPatterns" to listOf(
                mapOf(
                    "pattern" to "base/test/*",
                    "index" to "first"
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
        "" to listOf("a", "x")
    ),
    "ticket" to mapOf("dir" to "ticket")
)

class MpbConfigKtTest {

    @Test
    fun canParseAndLoadConfigs() {

        val config = parseArgsAndLoadConfig(listOf(
            "pull",
            "--debug",
            "--", "a", "b",
            "--args.a", "args.a",
            "--args.b", "args.b",
            "--branchPatterns[1].pattern", "p2",
            "--branchPatterns[1].index", "last"
        )) { ConfigMutableMapState(testConfig.asMutableMap()) {} }

        assertEquals(true, config.debug)
        assertEquals("pull", config.command)
        assertEquals(listOf("a", "b"), config.commonArgs)
        assertEquals(listOf("args.a"), config.args["a"])
        assertEquals(listOf("args.b"), config.args["b"])
        assertEquals(BranchPattern("p2", -1), config.branchPatterns[1])

    }

}
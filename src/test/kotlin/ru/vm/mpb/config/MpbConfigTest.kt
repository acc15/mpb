package ru.vm.mpb.config

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import ru.vm.mpb.config.loader.YamlLoader
import kotlin.test.*

val testConfig get() = mapOf(
    "name" to "test",
    "debug" to false,
    "baseDir" to "/baseDir",
    "branch" to mapOf(
        "default" to "master",
        "patterns" to listOf(
            mapOf(
                "input" to "^(\\d+)\$",
                "branch" to "^origin/(rc/3\\.\$1\\..*-sf)\$",
                "index" to "last"
            )
        )
    ),
    "build" to mapOf(
        "default" to mapOf(
            "env" to mapOf("ENV1" to "VALUE1"),
            "commands" to mapOf(
                "default" to listOf("c", "d", "e"),
                "nt" to listOf("c", "d", "e", "F")
            )
        ),
        "java17" to mapOf(
            "env" to mapOf("JAVA_HOME" to "/usr/lib/jvm/java-17-openjdk"),
            "commands" to mapOf(
                "default" to listOf("c", "d", "e"),
                "nt" to listOf("c", "d", "e", "F")
            )
        ),
        "yarn" to mapOf(
            "commands" to mapOf("default" to listOf("a", "b"))
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
                "patterns" to listOf(
                    mapOf(
                        "input" to "^test-(\\d+)\$",
                        "regex" to "^origin/(base/test/\$1)\$",
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
    "ticket" to mapOf("dir" to "ticket")
)

fun equalBranchPattern(b1: BranchPattern, b2: BranchPattern) =
    b1.input.pattern == b2.input.pattern &&
    b1.branch == b2.branch &&
    b1.index == b2.index

class MpbConfigTest {

    @BeforeTest
    fun setUp() {
        mockkObject(YamlLoader)
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun parse() {

        every { YamlLoader.load(MpbEnv.home.resolve("mpb.yaml")) } returns testConfig

        val config = MpbConfig.parse(
            "pull",
            "--debug",
            "--", "a", "b",
            "--args.a", "args.a",
            "--args.b", "args.b",
            "--branch.patterns[1].input", "i2",
            "--branch.patterns[1].branch", "p2",
            "--branch.patterns[1].index", "last"
        )

        assertEquals("test", config.name)
        assertEquals(true, config.debug)
        assertEquals("pull", config.args.command)
        assertEquals(listOf("a", "b"), config.args.common)
        assertEquals(listOf("args.a"), config.args.project["a"])
        assertEquals(listOf("args.b"), config.args.project["b"])

    }

    /*
    ### mpb.yaml
    config: b.yaml
    name: a

### b.yaml
    name: b
    profiles:
        a:
            config: c.yaml
            name: f # overrides 'c'
            # name: e # overrides 'f' from profile 'a' of c.yaml

### c.yaml
    name: c
    profiles:
        a:
            name: e

###
Merge order
# no profile - b.yaml [base], mpb.yaml [base]
# a profile - b.yaml [base], mpb.yaml [base], c.yaml [base], b.yaml [profile 'a'], c.yaml [profile 'a']

     */

 }
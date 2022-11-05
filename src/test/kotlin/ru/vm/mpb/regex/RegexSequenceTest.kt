package ru.vm.mpb.regex

import org.junit.jupiter.api.Test
import java.io.FileReader
import kotlin.test.*

class RegexSequenceTest {

    private val testString = """
        [INFO] --- buildplan-maven-plugin:1.5:list (default-cli) @ test-project ---
        [INFO] Build Plan for test-project:  
        --------------------------------------------------------------------------------------- 
        PLUGIN                 | PHASE                  | ID                    | GOAL         
        --------------------------------------------------------------------------------------- 
        maven-clean-plugin     | clean                  | default-clean         | clean         
        flatten-maven-plugin   | clean                  | flatten.clean         | clean         
        maven-resources-plugin | process-resources      | default-resources     | resources     
        flatten-maven-plugin   | process-resources      | flatten               | flatten       
        maven-compiler-plugin  | compile                | default-compile       | compile 
    """.trimIndent()

    private val planSequence = RegexSequence(listOf(
        Regex("^\\[INFO] --- buildplan-maven-plugin:.+?:list \\(.+?\\) @ (?<project>\\S+) ---$"),
        Regex("^\\S+\\s*\\|\\s*\\S+\\s*\\|\\s*(?!ID)(?<id>\\S+)\\s*\\|\\s*\\S+\\s*$")
    ), "\${project}@\${id}")

    private val execSequence = RegexSequence(listOf(
        Regex("\\[INFO] --- [^:]+:[^:]+:\\S+ \\((?<id>\\S+)\\) @ (?<project>\\S+) ---")
    ), "\${project}@\${id}")

    private fun findAllMatches(seq: RegexSequence, lines: Sequence<String>): List<String> {
        val list = mutableListOf<String>()
        seq.findAllMatches(lines, list::add)
        return list
    }

    @Test
    fun findMatches() {
        val lines = testString.split("\n").asSequence()
        val matches = findAllMatches(planSequence, lines)

        val expected = listOf(
            "test-project@default-clean",
            "test-project@flatten.clean",
            "test-project@default-resources",
            "test-project@flatten",
            "test-project@default-compile"
        )
        assertEquals(expected, matches)
    }

    @Test
    fun findMatchesMustReturnEmptyListIfFirstRegexNeverMatched() {
        val lines = testString.split("\n").drop(1).asSequence()
        val matches = findAllMatches(planSequence, lines)
        assertTrue(matches.isEmpty())
    }

    @Test
    @Ignore
    fun findMatchesInFile() {
        val plan = FileReader("src/main/examples/mvn_multi_plan.txt").use {
            findAllMatches(planSequence, it.buffered().lineSequence()).toSet()
        }
        val exec = FileReader("src/main/examples/mvn_multi_parallel.txt").use {
            findAllMatches(execSequence, it.buffered().lineSequence()).toSet()
        }
        assertEquals(plan, exec)
    }

}
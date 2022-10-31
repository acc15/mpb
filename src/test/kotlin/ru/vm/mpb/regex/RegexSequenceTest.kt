package ru.vm.mpb.regex

import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.Ignore
import kotlin.test.assertEquals

class RegexSequenceTest {

    private val TEST_STRING = """
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

    @Test
    fun findMatches() {
        val lines = TEST_STRING.split("\n")
        val state = mutableListOf<MatchGroupCollection>()
        val matches = mutableListOf<String>()
        for (line in lines) {
            matches.addAll(planSequence.findMatches(state, line))
        }

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
    @Ignore
    fun findMatchesInFile() {
        val planLines = File("mvn_multi_plan.txt").readLines()
        val execLines = File("mvn_multi_parallel.txt").readLines()
        val plan = planSequence.findAllMatches(planLines).toSet()
        val exec = execSequence.findAllMatches(execLines).toSet()
        assertEquals(plan, exec)
    }

}
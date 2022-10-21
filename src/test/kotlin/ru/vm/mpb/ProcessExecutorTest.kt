package ru.vm.mpb

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.test.Ignore

class ProcessExecutorTest {

    @Test
    fun exitCode() {
        assertEquals(0, ProcessExecutor(ProcessBuilder("echo", "l1\nl2")).wait())
        assertEquals(1, ProcessExecutor(ProcessBuilder("cat", "/tmp/__nonexist__")).wait())
    }

    @Test
    fun success() {
        assertTrue(ProcessExecutor(ProcessBuilder("echo", "l1\nl2")).success())
        assertFalse(ProcessExecutor(ProcessBuilder("cat", "/tmp/__nonexist__")).success())
    }

    @Test
    fun lines() {
        assertEquals(
            listOf("l1", "l2"),
            ProcessExecutor(ProcessBuilder("echo", "l1\nl2")).lines()
        )
    }

    @Test
    @Ignore
    fun branchList() {
        val testDir = System.getenv()["TEST_GIT_DIR"]!!
        ProcessExecutor(ProcessBuilder("git", "branch", "-r"))
            .dir(testDir)
            .lines().map { l -> l.substring(2) }
            .forEach { println(it) }
    }

}
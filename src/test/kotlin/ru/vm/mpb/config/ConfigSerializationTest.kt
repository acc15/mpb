package ru.vm.mpb.config

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import java.nio.file.Path




/*

@Serializable
data class BuildProgress(
    /** Command to run to get build plan (how many steps, and their names */
    val cmd: List<String>,

    /** Regex sequence to parse build plan */
    val plan: RegexSequence,

    /** Regex sequence to build execution */
    val build: RegexSequence
)

@Serializable
data class Build(
    /** Parent build configuration */
    val parent: String? = null,

    /**
     * Command line to launch tool
     *
     * Overrides `parent`->`tool` if not null */
    val tool: List<String>? = null,

    /**
     * Additional options for tool (useful to specify additional options without overriding `tool`)
     *
     * Overrides `parent`->`opts` if not null
     */
    val opts: List<String>? = null,

    /**
     * Command alias map
     *
     * Inherited from parent, merged by keys
     */
    val commands: Map<String, List<String>> = emptyMap(),

    /**
     * Environment variables
     *
     * Inherited from parent, merged by keys
     */
    val env: Map<String, String> = emptyMap(),

    /**
     * Tool build progress configuration
     *
     * Overrides `parent`->`progress` if not null */
    val progress: BuildProgress? = null
)

@Serializable
data class ProjectConfig(

    /**
     * Project directory. Relative to [PathConfig.projects]. Defaults to `${name}`
     */
    val dir: Path?,

    /**
     * Project log file path. Relative to [PathConfig.logs]. Defaults to `${name}.log`
     */
    val log: Path?,

    /** Dependencies */
    val deps: Set<String> = emptySet(),

    /** Build configuration name */
    val build: String = "default",

    /** Git configuration */
    val git: ProjectGitConfig = ProjectGitConfig()
)

@Serializable
data class PathConfig(
    /** Directory for build logs */
    val logs: Path = Path("log"),

    /** Base dir for all projects */
    val projects: Path = Path("")
)

@Serializable
data class MpbConfig(
    /** Configuration name (used for self-referencing) */
    val name: String,

    /** Called commands allowed to inherit STDOUT */
    val debug: Boolean = false,

    /** Run project tasks sequentially (not in parallel) */
    val seq: Boolean = false,

    /** Base paths */
    val path: PathConfig = PathConfig(),

    /** Global git configuration */
    val git: GitConfig = GitConfig(),

    /** Projects */
    val projects: Map<String, ProjectConfig> = emptyMap(),

    /** Build tools, options, environment and commands */
    val build: Map<String, Build> = emptyMap()
)


class ConfigSerializationTest {

    val serializer = Yaml(configuration = YamlConfiguration(strictMode = false, allowAnchorsAndAliases = true, ))

    @Test
    fun testDefault() {
        val parsedConfig = serializer.decodeFromString(NewMpbConfig.serializer(), """
                name: abc
            """.trimIndent())
        assertEquals(NewMpbConfig("abc"), parsedConfig)
    }

    @Test
    fun testOutput() {
        val parsedConfig = serializer.decodeFromString(NewMpbConfig.serializer(), """
                name: abc
                output: { plain: true, monochrome: true, width: 120 }
            """.trimIndent())
        assertEquals(NewMpbConfig("abc", output = OutputConfig(plain = true, monochrome = true, 120)), parsedConfig)
    }

}*/
package ru.vm.mpb.config

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.Serializable
import ru.vm.mpb.regex.RegexAsStringSerializer
import ru.vm.mpb.regex.RegexSequence
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.test.*

@Serializable
data class OutputConfig(
    /** Outputs all message lines, otherwise only current message (Status) is printed */
    val plain: Boolean = false,

    /** Disables colorful output */
    val monochrome: Boolean = false,

    /** Default terminal width */
    val width: Int = 80
)

@Serializable
data class BranchPattern(
    @Serializable(with = RegexAsStringSerializer::class)
    val input: Regex,
    val branch: String,
    val index: Int
)

@Serializable
data class GitConfig(
    /** Default branch name (for switching, etc) */
    val default: String = "master",

    /** List of branch patterns for quick switching to related branches */
    val patterns: List<BranchPattern> = emptyList(),

    /** Disables fetching */
    val noFetch: Boolean = false,

    /** Disables rebasing */
    val noRebase: Boolean = false,

    /** Ignored paths */
    val ignore: Set<Path> = emptySet(),

    /** Maximum session count (useful to limit maximum connection count to single server) */
    val maxSessions: Int = 0
)

@Serializable
data class ProjectGitConfig(
    /**
     * Default branch name (for switching, etc)
     *
     * Overrides top level [GitConfig.default]
     */
    val default: String? = null,

    /**
     * List of branch patterns for quick switching to related branches
     *
     * Overrides top level [GitConfig.patterns]
     */
    val patterns: List<BranchPattern>? = null,

    /**
     * Disables fetching
     *
     * Overrides top level [GitConfig.noFetch]
     */
    val noFetch: Boolean? = null,

    /**
     * Disables rebasing
     *
     * Overrides top level [GitConfig.noRebase]
     */
    val noRebase: Boolean? = null,

    /**
     * Ignored paths
     *
     * Overrides top level [GitConfig.ignore]
     */
    val ignore: Set<Path>? = null
)

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
     * Project directory
     *
     * Defaults to project name
     */
    val dir: Path? = null,

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
    val log: Path = Path("log"),

    /** Base dir for all projects */
    val project: Path = Path("")
)

@Serializable
data class NewMpbConfig(
    /** Configuration name (used for self-referencing) */
    val name: String,

    /** Called commands allowed to inherit STDOUT */
    val debug: Boolean = false,

    /** Run project tasks sequentially (not in parallel) */
    val seq: Boolean = false,

    /** Console output configuration */
    val output: OutputConfig = OutputConfig(),

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

}
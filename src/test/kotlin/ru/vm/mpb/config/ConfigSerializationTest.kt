package ru.vm.mpb.config

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ru.vm.mpb.regex.RegexSequence
import java.nio.file.Path
import kotlin.test.*

object RegexAsStringSerializer : KSerializer<Regex> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Regex", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Regex) { encoder.encodeString(value.pattern) }
    override fun deserialize(decoder: Decoder): Regex { return Regex(decoder.decodeString()) }
}

@Serializable
data class NewMpbConfig(
    /** Configuration name (used for self-referencing) */
    val name: String,

    /** Called commands allowed to inherit STDOUT */
    val debug: Boolean = false,

    /** Run project tasks sequentially (not in parallel) */
    val seq: Boolean = false,

    /** Console output configuration */
    val output: Output = Output(),

    /** Global git configuration */
    val git: Git /*,
    val projects: Map<String, Project>,
    val jira: Jira,
    val ticket: Ticket,
    val build: Map<String, Build>,*/
) {

    @Serializable
    data class Output(
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
    data class Git(
        /** Default branch name (for switching, etc) */
        val default: String? = null,

        /** List of branch patterns for quick switching to related branches */
        val patterns: List<BranchPattern> = emptyList(),

        /** Disables fetching */
        val noFetch: Boolean = false,

        /** Disables rebasing */
        val noRebase: Boolean = false,

        /** Ignored paths */
        val ignore: Set<Path> = emptySet()
    )

    data class Project(
        /** Base directory */
        val dir: Path,

        /** Dependencies */
        val deps: Set<String>,

        /** Build configuration */
        val build: BuildConfig,

        /** Git configuration */
        val git: Git,

        /** Project log file */
        val log: Path
    )

    data class Jira(
        val url: String,
        val project: String
    )

    data class Build(
        val use: String?,
        val tool: List<String>,
        val opt: List<String>,
        val commands: Map<String, List<String>>,
        val env: Map<String, String>,
        val progress: Progress
    )

    data class Progress(
        val cmd: List<String>,
        val plan: RegexSequence,
        val build: RegexSequence
    )

    data class Ticket(
        val dir: Path,
        val overwrite: Boolean
    )

}


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
        assertEquals(NewMpbConfig("abc", output = NewMpbConfig.Output(true, true, 120)), parsedConfig)
    }

}
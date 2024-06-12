package ru.vm.mpb.configuration

import kotlinx.serialization.Serializable

@Serializable
data class ToolConfig(
    /** Parent build tool configuration */
    val parent: String? = null,

    /**
     * Command line to launch tool
     *
     * Overrides [parent]->[tool] if not null */
    val tool: List<String>? = null,

    /**
     * Additional options for tool
     *
     * Useful for specifying additional options without overriding [tool] command line
     *
     * Overrides [parent]->[opts] if not null
     */
    val opts: List<String>? = null,

    /**
     * Tool commands map
     *
     * Inherits [parent]->[commands], merged by keys
     */
    val commands: Map<String, List<String>> = emptyMap(),

    /**
     * Tool environment variables
     *
     * Inherits [parent]->[env], merged by keys
     */
    val env: Map<String, String> = emptyMap(),

    /**
     * Tool build progress configuration
     *
     * Overrides [parent]->[progress] if not null
     */
    val progress: ToolProgressConfig? = null
)
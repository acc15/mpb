package ru.vm.mpb.config

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.fusesource.jansi.AnsiConsole
import ru.vm.mpb.config.loader.ConfigLoader
import ru.vm.mpb.config.loader.YamlLoader
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.test.*

class ConfigLoaderTest {

    @BeforeTest
    fun setUp() {
        mockkObject(YamlLoader)
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    private fun mockYamls(vararg entries: Pair<String, Any?>, relativeTo: Path? = MpbEnv.home) {
        for (f in entries) {
            val p = if (relativeTo != null) relativeTo.resolve(f.first) else Path(f.first)
            every { YamlLoader.load(p) } returns f.second
        }
    }

    @Test
    fun mustUseConfigParameter() {
        mockYamls("/test/config.yaml" to mapOf("name" to "custom"))

        val cfg = ConfigLoader().load("--config", "/test/config.yaml")
        assertEquals("custom", cfg.get("name").value)
    }

    @Test
    fun mergesParentConfig() {
        mockYamls(
            "mpb.yaml" to mapOf("config" to "/test/config.yaml"),
            "/test/config.yaml" to mapOf("name" to "custom"),
        )

        val cfg = ConfigLoader().load()
        assertEquals("custom", cfg.get("name").value)
    }

    @Test
    fun configPathsAreToCurrent() {
        mockYamls(
            "/test/root.yaml" to mapOf("config" to "custom.yaml"),
            "/test/custom.yaml" to mapOf("name" to "a")
        )

        val cfg = ConfigLoader().load("--config", "/test/root.yaml")
        assertEquals("a", cfg.get("name").value)
    }

    @Test
    fun configOverridesParent() {
        mockYamls(
            "mpb.yaml" to mapOf("config" to "b.yaml", "name" to "b"),
            "b.yaml" to mapOf("name" to "a")
        )

        val cfg = ConfigLoader().load()
        assertEquals("b", cfg.get("name").value)
    }

    @Test
    fun mergesConfigFromProfile() {
        mockYamls(
            "mpb.yaml" to mapOf(
                "profiles" to mapOf(
                    "a" to mapOf("config" to "b.yaml")
                )
            ),
            "b.yaml" to mapOf("b" to true)
        )

        val cfg = ConfigLoader().load("--profile", "a")
        assertEquals(true, cfg.get("b").value)
    }

    @Test
    fun configOverridesConfigFromProfile() {
        mockYamls(
            "mpb.yaml" to mapOf(
                "profiles" to mapOf(
                    "a" to mapOf(
                        "config" to "b.yaml",
                        "b" to false
                    )
                )
            ),
            "b.yaml" to mapOf("b" to true)
        )

        val cfg = ConfigLoader().load("--profile", "a")
        assertEquals(false, cfg.get("b").value)
    }

    @Test
    fun mustAvoidLongCircularLoops() {
        mockYamls(
            "a.yaml" to mapOf("config" to "b.yaml"),
            "b.yaml" to mapOf("config" to "c.yaml"),
            "c.yaml" to mapOf("config" to "d.yaml"),
            "d.yaml" to mapOf("config" to "e.yaml"),
            "e.yaml" to mapOf("config" to "a.yaml"),
            relativeTo = null
        )

        val cfg = ConfigLoader().load("--config", "a.yaml")
        assertEquals(emptyMap<String, Any>(), cfg.value)
    }

    @Test
    fun mustAvoidLoopsInSelfProfiles() {
        mockYamls(
            "a.yaml" to mapOf(
                "profiles" to mapOf(
                    "a" to mapOf("config" to "a.yaml")
                )
            ), relativeTo = null
        )

        val cfg = ConfigLoader().load("--config", "a.yaml", "--profile", "a")
        assertNotNull(cfg.value)
    }

    @Test
    fun mustAvoidLoopsInProfiles() {
        mockYamls(
            "a.yaml" to mapOf("config" to "b.yaml"),
            "b.yaml" to mapOf(
                "profiles" to mapOf(
                    "a" to mapOf("config" to "c.yaml")
                )
            ),
            "c.yaml" to mapOf(
                "profiles" to mapOf(
                    "a" to mapOf("config" to "a.yaml")
                )
            ),
            relativeTo = null
        )

        val cfg = ConfigLoader().load("--config", "a.yaml", "--profile", "a")
        assertNotNull(cfg.value)
    }


    @Test
    fun mustIgnoreCircularLoopsInInactiveProfiles() {
        mockYamls(
            "a.yaml" to mapOf(
                "test" to true,
                "profiles" to mapOf("a" to mapOf("config" to "b.yaml"))
            ),
            "b.yaml" to mapOf("config" to "c.yaml"),
            "c.yaml" to mapOf("config" to "d.yaml"),
            "d.yaml" to mapOf("config" to "e.yaml"),
            "e.yaml" to mapOf("config" to "a.yaml"),
            relativeTo = null
        )

        val cfg = ConfigLoader().load("--config", "a.yaml")
        assertEquals(true, cfg.get("test").value)
    }

    @Test
    fun mergeSequence() {

        mockYamls(
            "parent-profile.yaml" to mapOf(
                "parent-profile-base" to true,
                "profiles" to mapOf(
                    "a" to mapOf(
                        "parent-profile-profile" to true
                    )
                )
            ),
            "parent.yaml" to mapOf(
                "parent-base" to true,
                "profiles" to mapOf(
                    "a" to mapOf(
                        "config" to "parent-profile.yaml",
                        "parent-profile" to true
                    )
                )
            ),
            "mpb.yaml" to mapOf(
                "config" to "parent.yaml",
                "mpb" to true,
                "profiles" to mapOf(
                    "a" to mapOf(
                        "config" to "profile.yaml",
                        "base-profile" to true
                    )
                )
            ),
            "profile.yaml" to mapOf(
                "profile-base" to true,
                "profiles" to mapOf(
                    "a" to mapOf(
                        "profile-profile" to true
                    )
                )
            )
        )

        val c1 = ConfigLoader().load("--profile", "a")
        val keys = c1.map.keys.toList()
        assertContentEquals(listOf(
            "parent-base",
            "profiles",
            "mpb",
            "parent-profile-base",
            "parent-profile-profile",
            "parent-profile",
            "profile-base",
            "profile-profile",
            "base-profile"
        ), keys)

    }

    @Test
    fun mustUseBaseForLoadingConfigs() {

        mockYamls(
            "mpb.yaml" to mapOf("base" to "/custom-base", "config" to "mpb.yaml"),
            "/custom-base/mpb.yaml" to mapOf("custom-base" to true)
        )

        val cfg = ConfigLoader().load()
        assertEquals(true, cfg.get("custom-base").value)

    }

}
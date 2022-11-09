package ru.vm.mpb.config

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path

object MpbEnv {

    val profiles: Set<String> = System.getenv("MPB_PROFILE")?.split(" ")?.let { LinkedHashSet(it) }.orEmpty()
    val cwd = Path("").toAbsolutePath()
    val home = determineAppHome()
    val cd = Path(System.getProperty("java.io.tmpdir"), "mpb_cd.txt")

    private fun determineAppHome(): Path {
        val envHome = System.getenv("MPB_HOME")
        if (envHome != null) {
            return Path(envHome)
        }

        val sourcePath = MpbEnv::class.java.protectionDomain.codeSource.location.path
        if (sourcePath.endsWith(".jar")) {
            val sourceFile = Path(sourcePath)
            if (Files.exists(sourceFile)) {
                return sourceFile.parent.parent
            }
        }

        return cwd
    }

}
package ru.vm.mpb.config

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path

object MpbPath {

    val cwd = Path("").toAbsolutePath()
    val home = determineAppHome()

    private fun determineAppHome(): Path {
        val envHome = System.getenv("MPB_HOME")
        if (envHome != null) {
            return Path(envHome)
        }

        val sourcePath = MpbPath::class.java.protectionDomain.codeSource.location.path
        if (sourcePath.endsWith(".jar")) {
            val sourceFile = Path(sourcePath)
            if (Files.exists(sourceFile)) {
                return sourceFile.parent.parent
            }
        }

        return cwd
    }

}
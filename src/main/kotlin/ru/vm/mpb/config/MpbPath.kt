package ru.vm.mpb.config

import java.io.File

object MpbPath {

    val cwd = File("").absoluteFile
    val home = determineAppHome()

    private fun determineAppHome(): File {
        val envHome = System.getenv("MPB_HOME")
        if (envHome != null) {
            return File(envHome)
        }

        val sourcePath = MpbPath::class.java.protectionDomain.codeSource.location.path
        if (sourcePath.endsWith(".jar")) {
            val sourceFile = File(sourcePath)
            if (sourceFile.exists()) {
                return sourceFile.parentFile.parentFile
            }
        }

        return cwd
    }

}
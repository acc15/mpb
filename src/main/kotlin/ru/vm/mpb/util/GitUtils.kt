package ru.vm.mpb.util

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File
import java.nio.file.Path

fun makeGit(dir: File): Git {
    val repo = FileRepositoryBuilder().setWorkTree(dir).findGitDir(dir).build()
    return Git(repo)
}
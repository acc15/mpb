package ru.vm.mpb.util

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.nio.file.Path

fun makeGit(dir: Path): Git {
    val file = dir.toFile()
    val repo = FileRepositoryBuilder().setWorkTree(file).findGitDir(file).build()
    return Git(repo)
}
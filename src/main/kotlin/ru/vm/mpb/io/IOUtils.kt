package ru.vm.mpb.util

import kotlinx.coroutines.*
import ru.vm.mpb.io.RedirectingInputStream
import ru.vm.mpb.io.SynchronizedOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList
import kotlin.io.path.*

fun deepDelete(path: Path) {
    val q = ArrayList<Pair<Path, Boolean>>()
    q.add(path to false)
    while (q.isNotEmpty()) {

        val (p, clean) = q.removeLast()
        if (clean) {
            p.deleteExisting()
            q.removeLast()
            continue
        }

        q.add(p to true)
        for (c in p) {
            if (c.isDirectory()) {
                q.add(c to false)
            } else {
                c.deleteExisting()
            }
        }
    }
}

fun deepMove(src: Path, dst: Path, replaceExisting: Boolean = true) {

    val options: Array<CopyOption> = if (replaceExisting) arrayOf(StandardCopyOption.REPLACE_EXISTING) else emptyArray()
    val q = ArrayList<Pair<Path, Path?>>()
    q.add(src to dst)

    while (q.isNotEmpty()) {
        val (srcPath, dstPath) = q.removeLast()

        if (dstPath == null) {
            srcPath.deleteExisting()
            continue
        }

        if (!srcPath.isDirectory()) {
            if (replaceExisting && dstPath.isDirectory()) {
                deepDelete(dstPath)
            }
            Files.move(srcPath, dstPath, *options)
            continue
        }

        if (!dstPath.isDirectory()) {
            if (replaceExisting) {
                dstPath.deleteIfExists()
            }
            dstPath.createDirectory()
        }

        q.add(srcPath to null)
        srcPath.forEachDirectoryEntry {
            q.add(it to dstPath.resolve(it.fileName))
        }
    }

}

typealias ReadCallback = (ByteArray, Int) -> Boolean

fun readFully(inputStream: InputStream, onRead: ReadCallback) {
    val buf = ByteArray(8192)
    while (true) {
        val sz = inputStream.read(buf)
        if (sz < 0 || !onRead(buf, sz)) {
            break
        }
    }
}

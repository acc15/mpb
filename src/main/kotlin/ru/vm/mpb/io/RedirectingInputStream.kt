package ru.vm.mpb.io

import java.io.FilterInputStream
import java.io.InputStream
import java.io.OutputStream

class RedirectingInputStream(inputStream: InputStream, val sink: OutputStream): FilterInputStream(inputStream) {

    override fun read(): Int {
        val byte = super.read()
        if (byte >= 0) {
            sink.write(byte)
        }
        return byte
    }

    override fun read(b: ByteArray): Int {
        return read(b, 0, b.size)
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val bytesRead = super.read(b, off, len)
        if (bytesRead > 0) {
            sink.write(b, off, bytesRead)
        }
        return bytesRead
    }
}
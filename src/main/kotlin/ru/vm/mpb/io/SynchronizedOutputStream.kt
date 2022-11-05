package ru.vm.mpb.io

import java.io.FilterOutputStream
import java.io.OutputStream

class SynchronizedOutputStream(sink: OutputStream): FilterOutputStream(sink) {
    @Synchronized override fun close() = super.close()
    @Synchronized override fun flush() = super.flush()
    @Synchronized override fun write(b: Int) = super.write(b)
    @Synchronized override fun write(b: ByteArray) = super.write(b)
    @Synchronized override fun write(b: ByteArray, off: Int, len: Int) = super.write(b, off, len)
}
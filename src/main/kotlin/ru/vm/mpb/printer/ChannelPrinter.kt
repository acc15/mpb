package ru.vm.mpb.printer

import kotlinx.coroutines.channels.SendChannel

class ChannelPrinter(val channel: SendChannel<PrintData>): Printer, AutoCloseable {
    override fun print(data: PrintData) {
        channel.trySend(data)
    }

    override fun close() {
        channel.close()
    }
}
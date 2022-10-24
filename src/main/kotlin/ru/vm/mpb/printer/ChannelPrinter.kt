package ru.vm.mpb.printer

import kotlinx.coroutines.channels.Channel

class ChannelPrinter(val channel: Channel<PrintData>): Printer {
    override suspend fun print(data: PrintData) {
        channel.send(data)
    }

    override fun close() {
        channel.close()
    }
}
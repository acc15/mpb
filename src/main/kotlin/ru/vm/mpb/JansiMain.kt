package ru.vm.mpb

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.AnsiConsole

suspend fun withJansi(callback: suspend () -> Unit) {
    AnsiConsole.systemInstall()
    try {
        callback()
    } finally {
        AnsiConsole.systemUninstall()
    }
}

fun getTerminalWidth(): Int {
    val w = AnsiConsole.getTerminalWidth();
    return if (w <= 0) 80 else w
}

suspend fun printProgress(key: String, channel: SendChannel<OutputData>, delay: Long) {
    val maxValue = 100
    for (i in 0..maxValue) {
        val width = 80 // getTerminalWidth()
        val position = i * width / maxValue
        channel.send(OutputData("${"#".repeat(position)}${"-".repeat(width - position)}", key))
        delay(delay)
    }
}

data class OutputData(
    val msg: String,
    val key: String? = null
)

@OptIn(ObsoleteCoroutinesApi::class)
fun main(args: Array<String>) = runBlocking {

    withJansi {

        val outputActor = actor<OutputData> {
            val keyLineNumbers = mutableMapOf<String, Int>()
            var line = 0
            for (msg in channel) {
                val offset = line - if (msg.key != null) keyLineNumbers.computeIfAbsent(msg.key) { line } else line
                val text = "${msg.msg} ${msg.key} O:$offset;L:$line"
                if (offset != 0) {
                    print(Ansi.ansi().cursorUpLine(offset).a(text).cursorDownLine(offset))
                } else {
                    println(text)
                    ++line
                }
            }
        }


        listOf(
            launch {
                printProgress("a", outputActor, 100)
            },
            launch {
                for (i in 0..20) {
                    outputActor.send(OutputData("test"))
                    delay(50)
                }
            },
            launch {
                printProgress("b", outputActor, 200)
            }
        ).joinAll()

        outputActor.close()
    }
}

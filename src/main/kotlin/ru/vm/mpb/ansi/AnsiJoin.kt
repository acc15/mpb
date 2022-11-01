package ru.vm.mpb.ansi

import org.fusesource.jansi.Ansi

typealias AnsiAppender<T> = (Ansi, T) -> Unit

val defaultAnsiAppender: AnsiAppender<Any?> = { x, v -> x.a(v) }

fun <T> boldAppender(consumer: AnsiAppender<T> = defaultAnsiAppender): AnsiAppender<T> = { a, v ->
    a.bold()
    consumer(a, v)
    a.boldOff()
}

fun <T> Ansi.join(
    coll: Iterable<T>,
    separator: String = ", ",
    consumer: (Ansi, T) -> Unit = defaultAnsiAppender
): Ansi {
    val iter = coll.iterator()
    if (!iter.hasNext()) {
        return this
    }
    while (true) {
        consumer(this, iter.next())
        if (!iter.hasNext()) {
            break
        }
        a(separator)
    }
    return this
}

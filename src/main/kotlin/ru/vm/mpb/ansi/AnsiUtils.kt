package ru.vm.mpb.ansi

import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.Consumer
import org.fusesource.jansi.AnsiConsole
import java.util.function.Supplier

fun <T> withAnsi(callback: () -> T): T {
    try {
        AnsiConsole.systemInstall()
        return callback()
    } finally {
        AnsiConsole.systemUninstall()
    }
}

private object NoAnsiSupplier: Supplier<Ansi> {
    @Suppress("UNCHECKED_CAST")
    private val ctor = (Class.forName("${Ansi::class.qualifiedName}\$NoAnsi") as Class<Ansi>)
        .getConstructor()
        .also { it.trySetAccessible() }

    override fun get(): Ansi = ctor.newInstance()
}

fun ansi() = Ansi()
fun noAnsi() = NoAnsiSupplier.get()

fun ansi(enabled: Boolean) = if (enabled) ansi() else noAnsi()

val ESC_REGEX = Regex(
    "[\\u001B\\u009B][\\[\\]()#;?]*(?:(?:(?:;[-a-zA-Z\\d/#&.:=?%@~_]+)*|" +
    "[a-zA-Z\\d]+(?:;[-a-zA-Z\\d/#&.:=?%@~_]*)*)?\\u0007|" +
    "(?:\\d{1,4}(?:;\\d{0,4})*)?[\\dA-PR-TZcf-nq-uy=><~])"
)

fun <T> Ansi.filterJoin(coll: Iterable<T>, separator: String = ", ", consumer: (Ansi, T) -> Boolean): Ansi {
    val iter = coll.iterator()
    if (!iter.hasNext()) {
        return this
    }
    while (true) {
        val item = iter.next()
        if (!consumer(this, item)) {
            continue
        }
        if (!iter.hasNext()) {
            break
        }
        a(separator)
    }
    return this
}

fun <T> Ansi.join(coll: Iterable<T>, separator: String = ", ", consumer: (Ansi, T) -> Unit) =
    filterJoin(coll, separator) { a, b -> consumer(a, b); true }

fun Ansi.applyIf(condition: Boolean, callback: Consumer): Ansi {
    if (condition) {
        apply(callback)
    }
    return this
}

fun stripAnsi(str: String): String {
    return ESC_REGEX.replace(str, "")
}
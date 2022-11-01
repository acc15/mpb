package ru.vm.mpb.ansi

import org.fusesource.jansi.Ansi
import java.lang.StringBuilder
import java.util.function.Supplier

interface AnsiFactory: Supplier<Ansi> {
    fun get(parent: Ansi): Ansi
    fun get(sb: StringBuilder): Ansi
}

private object AnsiEnabledFactory: AnsiFactory {
    override fun get() = Ansi()
    override fun get(parent: Ansi) = Ansi(parent)
    override fun get(sb: StringBuilder) = Ansi(sb)
}

private object AnsiDisabledFactory: AnsiFactory {
    @Suppress("UNCHECKED_CAST")
    private val noAnsiClass = Class.forName("${Ansi::class.qualifiedName}\$NoAnsi") as Class<Ansi>
    private val noArgCtor = noAnsiClass.getConstructor().also { it.trySetAccessible() }
    private val sbCtor = noAnsiClass.getConstructor(StringBuilder::class.java).also { it.trySetAccessible() }
    override fun get(): Ansi = noArgCtor.newInstance()
    override fun get(parent: Ansi): Ansi = get(StringBuilder(stripAnsi(parent.toString())))
    override fun get(sb: StringBuilder): Ansi = sbCtor.newInstance(sb)
}

fun ansi(enabled: Boolean): AnsiFactory = if (enabled) AnsiEnabledFactory else AnsiDisabledFactory

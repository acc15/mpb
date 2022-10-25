package ru.vm.mpb.util

import java.time.Duration

enum class DurationTimeUnit(
    val suffix: String,
    val div: Long = 1,
    val mod: Long = 0,
    val fetch: (Duration) -> Long = Duration::getSeconds
) {
    MILLIS("ms", fetch = { it.nano.toLong() }, div = 1000000),
    SECONDS("s", 1, 60),
    MINUTES("m", 60, 60),
    HOURS("h", 60 * 60, 60),
    DAYS("d", 60 * 60 * 24);

    fun format(d: Duration): String {
        val v = fetch(d)
        val x = if (div > 1) v / div else v
        val m = if (mod > 0) x % mod else x
        return if (m > 0) "${m}${suffix}" else ""
    }
}

val Duration.prettyString: String get() = DurationTimeUnit.values().reversed()
    .map { it.format(this) }
    .filter { it.isNotEmpty() }
    .joinToString(" ")

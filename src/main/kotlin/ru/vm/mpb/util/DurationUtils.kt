package ru.vm.mpb.util

import java.lang.StringBuilder
import java.time.Duration

enum class DurationTimeUnit(
    val suffix: String,
    val div: Long = 1,
    val mod: Long = 1,
    val fetch: (Duration) -> Long = Duration::getSeconds
) {
    MILLIS("ms", fetch = { it.nano.toLong() }, div = 1000000),
    SECONDS("s", 1, 60),
    MINUTES("m", SECONDS.div * 60, 60),
    HOURS("h", MINUTES.div * 60, 24),
    DAYS("d", HOURS.div * 24);

    fun appendTo(duration: Duration, builder: StringBuilder) {
        val v = fetch(duration)
        val d = if (div > 1) v / div else v
        val m = if (mod > 1) d % mod else d
        if (m <= 0) {
            return
        }
        if (builder.isNotEmpty()) {
            builder.append(' ')
        }
        builder.append("$m$suffix")
    }
}

val Duration.prettyString: String get() {
    val b = StringBuilder()
    val values = DurationTimeUnit.values()
    for (index in values.indices.reversed()) {
        values[index].appendTo(this, b)
    }
    return b.toString()
}

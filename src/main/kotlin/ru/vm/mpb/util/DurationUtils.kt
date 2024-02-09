package ru.vm.mpb.util

import org.fusesource.jansi.Ansi.Consumer
import ru.vm.mpb.ansi.join
import kotlin.time.Duration

enum class DurationTimeUnit(
    val suffix: String,
    val div: Long = 1,
    val mod: Long = 1
) {
    MILLIS("ms", 1, 1000),
    SECONDS("s", 1000, 60),
    MINUTES("m", SECONDS.div * 60, 60),
    HOURS("h", MINUTES.div * 60, 24),
    DAYS("d", HOURS.div * 24);

    fun getUnits(duration: Duration): Long {
        val v = duration.inWholeMilliseconds
        val d = if (div > 1) v / div else v
        return if (mod > 1) d % mod else d
    }
}

val Duration.pretty get() = Consumer { a ->
    a.join(DurationTimeUnit.entries.reversed().map { it to it.getUnits(this) }.filter { it.second > 0 }, " ")
        { x, v -> x.a(v.second).a(v.first.suffix) }
}

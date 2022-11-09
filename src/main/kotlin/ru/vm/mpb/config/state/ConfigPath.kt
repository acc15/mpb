package ru.vm.mpb.config.state

object ConfigPath {

    fun parseSegment(segment: String, maybeIndex: Boolean): Any {
        if (maybeIndex) {
            val index = segment.trim().toIntOrNull()
            if (index != null) {
                return index
            }
        }
        return kebabCaseToLowerCamelCase(segment)
    }

    fun parse(str: String): List<Any> {

        val segments = mutableListOf<Any>()
        val segment = StringBuilder()
        var indexDepth = 0

        fun flush(allowEmpty: Boolean) {
            if (segment.isEmpty() && !allowEmpty) {
                return
            }
            segments.add(parseSegment(segment.toString(), indexDepth == 1))
            segment.clear()
        }

        for ((i, ch) in str.withIndex()) {
            when {
                indexDepth == 0 && ch == '.' -> {
                    flush(i == 0 || str[i - 1] != ']')
                    continue
                }
                indexDepth == 0 && ch == '[' -> {
                    flush(i > 0 && str[i - 1] == '.')
                    ++indexDepth
                    continue
                }
                indexDepth > 0 && ch == '[' -> {
                    ++indexDepth
                }
                indexDepth > 1 && ch == ']' -> {
                    --indexDepth
                }
                indexDepth == 1 && ch == ']' -> {
                    flush(true)
                    indexDepth = 0
                    continue
                }
            }
            segment.append(ch)
        }
        flush(false)

        return segments
    }

    fun kebabCaseToLowerCamelCase(str: CharSequence): String {
        val builder = StringBuilder()
        var toUpper = false
        for (c in str) {
            if (c == '-') {
                toUpper = true
                continue
            }
            builder.append(if (toUpper) c.uppercaseChar() else c)
            toUpper = false
        }
        return builder.toString()
    }

}
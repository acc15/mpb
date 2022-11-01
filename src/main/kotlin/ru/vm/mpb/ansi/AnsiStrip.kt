package ru.vm.mpb.ansi

private val ESC_REGEX = Regex(
    "[\\u001B\\u009B][\\[\\]()#;?]*(?:(?:(?:;[-a-zA-Z\\d/#&.:=?%@~_]+)*|" +
            "[a-zA-Z\\d]+(?:;[-a-zA-Z\\d/#&.:=?%@~_]*)*)?\\u0007|" +
            "(?:\\d{1,4}(?:;\\d{0,4})*)?[\\dA-PR-TZcf-nq-uy=><~])"
)

fun stripAnsi(str: String) = ESC_REGEX.replace(str, "")

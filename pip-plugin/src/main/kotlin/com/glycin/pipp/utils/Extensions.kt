package com.glycin.pipp.utils

object Extensions {

    fun String.getBetween(start: String, end: String): String {
        val startEscaped = Regex.escape(start)
        val endEscaped = Regex.escape(end)
        val regex = Regex("$startEscaped(.*?)$endEscaped", RegexOption.DOT_MATCHES_ALL)
        return regex.findAll(this).joinToString("") { it.groupValues[1] }
    }

    fun String.getAndRemoveBetween(start: String, end: String): Pair<String, String> {
        val startEscaped = Regex.escape(start)
        val endEscaped = Regex.escape(end)
        val regex = Regex("$startEscaped(.*?)$endEscaped", RegexOption.DOT_MATCHES_ALL)
        val found = StringBuilder()
        val replaced = regex.replace(this) {
            found.append(it.groupValues[1])
            ""
        }
        return found.toString() to replaced
    }

    fun String.getAndRemoveCodeBlock(): Pair<String, String> {
        val regex = Regex("^```(?:\\w+)?\\s*\\n(.*?)(?=^```)```", setOf( RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE))
        val found = StringBuilder()
        val replaced = regex.replace(this) {
            found.append(it.groupValues[1])
            ""
        }
        return found.toString() to replaced
    }
}
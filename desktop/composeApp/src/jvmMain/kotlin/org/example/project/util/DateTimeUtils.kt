package org.example.project.util

fun formatVnDateTime(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return try {
        // Strip fractional seconds, timezone offset, Z suffix
        val clean = raw.substringBefore('.').substringBefore('+').substringBefore('Z').trim()
        if (clean.length < 10) return raw.replace('T', ' ').take(16)
        val d = clean.substring(8, 10)
        val m = clean.substring(5, 7)
        val y = clean.substring(0, 4)
        val time = if (clean.length >= 16) clean.substring(11, 16) else ""
        if (time.isNotEmpty()) "$d/$m/$y $time" else "$d/$m/$y"
    } catch (e: Exception) {
        raw.replace('T', ' ').take(16)
    }
}

fun formatVnDate(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return try {
        val clean = raw.substringBefore('T').substringBefore(' ')
        if (clean.length < 10) return raw.take(10)
        "${clean.substring(8, 10)}/${clean.substring(5, 7)}/${clean.substring(0, 4)}"
    } catch (e: Exception) {
        raw.take(10)
    }
}

fun formatUtcToVnDateTime(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return try {
        val clean = raw.substringBefore('.').substringBefore('+').substringBefore('Z').trim()
        if (clean.length < 16) return formatVnDateTime(raw)
        var hour  = clean.substring(11, 13).toInt() + 7
        var day   = clean.substring(8,  10).toInt()
        var month = clean.substring(5,  7).toInt()
        var year  = clean.substring(0,  4).toInt()
        val min   = clean.substring(14, 16)
        if (hour >= 24) {
            hour -= 24
            val maxDay = when (month) {
                1, 3, 5, 7, 8, 10, 12 -> 31
                4, 6, 9, 11 -> 30
                2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
                else -> 30
            }
            if (++day > maxDay) { day = 1; if (++month > 12) { month = 1; year++ } }
        }
        "%02d/%02d/%04d %02d:%s".format(day, month, year, hour, min)
    } catch (e: Exception) {
        formatVnDateTime(raw)
    }
}

package com.example.bacalog.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Utilitas validasi input murni Kotlin (JVM-safe) untuk menjaga konsistensi aturan validasi UI & ViewModel.
 */
object ValidationUtils {

    private val ISO_DATE_REGEX = Regex("""^\d{4}-\d{2}-\d{2}$""")
    private val EMAIL_REGEX = Regex("""^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""")

    private fun createUtcIsoFormatter(): SimpleDateFormat {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    fun isValidIsoDate(dateStr: String): Boolean {
        val trimmed = dateStr.trim()
        if (!ISO_DATE_REGEX.matches(trimmed)) return false
        return try {
            val sdf = createUtcIsoFormatter()
            val parsed = sdf.parse(trimmed) ?: return false
            sdf.format(parsed) == trimmed
        } catch (_: Exception) {
            false
        }
    }

    fun formatUtcMillisToIsoDate(utcMillis: Long): String {
        return createUtcIsoFormatter().format(Date(utcMillis))
    }

    fun parseIsoDateToUtcMillis(dateStr: String): Long? {
        val trimmed = dateStr.trim()
        if (!isValidIsoDate(trimmed)) return null
        return try {
            createUtcIsoFormatter().parse(trimmed)?.time
        } catch (_: Exception) {
            null
        }
    }

    fun isValidEmail(email: String): Boolean {
        return EMAIL_REGEX.matches(email.trim())
    }

    fun isValidPassword(password: String): Boolean {
        return password.trim().length >= 6
    }

    fun parsePositiveInt(value: String): Int? {
        return value.trim().toIntOrNull()?.takeIf { it > 0 }
    }

    fun calculateProgressRatio(achieved: Int, target: Int): Float {
        return if (target > 0) {
            (achieved.toFloat() / target.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }
}

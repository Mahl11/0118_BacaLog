package com.example.bacalog.util

import com.example.bacalog.data.model.ProgresDto
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max

/**
 * Representasi satu hari pada grafik ringkas aktivitas membaca tujuh hari terakhir.
 */
data class DailyProgressPoint(
    val isoDate: String,
    val dayLabel: String,
    val totalPages: Int,
    val barFraction: Float
)

/**
 * Utilitas murni Kotlin untuk menghitung ringkasan halaman baca harian selama 7 hari terakhir.
 */
object WeeklyProgressHelper {

    private fun createFormatter(timeZone: TimeZone): SimpleDateFormat {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            isLenient = false
            this.timeZone = timeZone
        }
    }

    fun getTodayIsoDate(): String {
        return createFormatter(TimeZone.getDefault()).format(Date())
    }

    fun buildLast7Days(
        progresList: List<ProgresDto>,
        referenceDateIso: String = getTodayIsoDate()
    ): List<DailyProgressPoint> {
        val utcTz = TimeZone.getTimeZone("UTC")
        val utcMillis = ValidationUtils.parseIsoDateToUtcMillis(referenceDateIso)
            ?: ValidationUtils.parseIsoDateToUtcMillis(getTodayIsoDate())
            ?: System.currentTimeMillis()

        val pagesByDate = progresList
            .groupBy { it.tanggalBaca.trim() }
            .mapValues { (_, items) -> items.sumOf { it.halamanDicapai.coerceAtLeast(0) } }

        val formatter = createFormatter(utcTz)
        val cal = Calendar.getInstance(utcTz).apply {
            timeInMillis = utcMillis
            add(Calendar.DAY_OF_MONTH, -6)
        }

        val rawDays = mutableListOf<Triple<String, String, Int>>()
        for (i in 0 until 7) {
            val iso = formatter.format(cal.time)
            val label = mapIndonesianDayLabel(cal.get(Calendar.DAY_OF_WEEK))
            val pages = pagesByDate[iso] ?: 0
            rawDays.add(Triple(iso, label, pages))
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        val maxPages = max(1, rawDays.maxOfOrNull { it.third } ?: 0)
        return rawDays.map { (iso, label, total) ->
            val fraction = if (total > 0) {
                (total.toFloat() / maxPages.toFloat()).coerceIn(0.12f, 1f)
            } else {
                0f
            }
            DailyProgressPoint(
                isoDate = iso,
                dayLabel = label,
                totalPages = total,
                barFraction = fraction
            )
        }
    }

    fun totalPagesInWindow(points: List<DailyProgressPoint>): Int {
        return points.sumOf { it.totalPages }
    }

    private fun mapIndonesianDayLabel(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "Min"
            Calendar.MONDAY -> "Sen"
            Calendar.TUESDAY -> "Sel"
            Calendar.WEDNESDAY -> "Rab"
            Calendar.THURSDAY -> "Kam"
            Calendar.FRIDAY -> "Jum"
            Calendar.SATURDAY -> "Sab"
            else -> "-"
        }
    }
}

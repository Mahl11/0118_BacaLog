package com.example.bacalog

import com.example.bacalog.data.model.ProgresDto
import com.example.bacalog.util.WeeklyProgressHelper
import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklyProgressHelperTest {

    @Test
    fun `buildLast7Days menghasilkan rentang tujuh hari berurutan dengan label hari Indonesia`() {
        val result = WeeklyProgressHelper.buildLast7Days(
            progresList = emptyList(),
            referenceDateIso = "2026-11-28"
        )

        assertEquals(7, result.size)
        assertEquals("2026-11-22", result.first().isoDate)
        assertEquals("Min", result.first().dayLabel)
        assertEquals("2026-11-28", result.last().isoDate)
        assertEquals("Sab", result.last().dayLabel)
        assertEquals(0, WeeklyProgressHelper.totalPagesInWindow(result))
    }

    @Test
    fun `buildLast7Days menjumlahkan log tanggal sama dan mengabaikan log di luar rentang`() {
        val sampleLogs = listOf(
            ProgresDto(id = 1, tanggalBaca = "2026-11-21", halamanDicapai = 50), // di luar 7 hari
            ProgresDto(id = 2, tanggalBaca = "2026-11-24", halamanDicapai = 15),
            ProgresDto(id = 3, tanggalBaca = "2026-11-24", halamanDicapai = 25), // total Sel = 40
            ProgresDto(id = 4, tanggalBaca = "2026-11-28", halamanDicapai = 20)  // Sab = 20
        )

        val result = WeeklyProgressHelper.buildLast7Days(
            progresList = sampleLogs,
            referenceDateIso = "2026-11-28"
        )

        val selPoint = result.first { it.isoDate == "2026-11-24" }
        val sabPoint = result.first { it.isoDate == "2026-11-28" }
        val minPoint = result.first { it.isoDate == "2026-11-22" }

        assertEquals(40, selPoint.totalPages)
        assertEquals(1f, selPoint.barFraction, 0.0001f)
        assertEquals(20, sabPoint.totalPages)
        assertEquals(0.5f, sabPoint.barFraction, 0.0001f)
        assertEquals(0, minPoint.totalPages)
        assertEquals(0f, minPoint.barFraction, 0.0001f)
        assertEquals(60, WeeklyProgressHelper.totalPagesInWindow(result))
    }
}

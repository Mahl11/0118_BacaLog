package com.example.bacalog

import com.example.bacalog.util.ValidationUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun `isValidIsoDate menerima tanggal kalender yang valid termasuk tahun kabisat`() {
        assertTrue(ValidationUtils.isValidIsoDate("2026-11-28"))
        assertTrue(ValidationUtils.isValidIsoDate("2024-02-29"))
        assertTrue(ValidationUtils.isValidIsoDate(" 2026-12-29 "))
    }

    @Test
    fun `isValidIsoDate menolak tanggal tidak valid di kalender atau format salah`() {
        assertFalse(ValidationUtils.isValidIsoDate("2026-02-29"))
        assertFalse(ValidationUtils.isValidIsoDate("2026-02-30"))
        assertFalse(ValidationUtils.isValidIsoDate("2026-13-01"))
        assertFalse(ValidationUtils.isValidIsoDate("2026-00-10"))
        assertFalse(ValidationUtils.isValidIsoDate("28-11-2026"))
        assertFalse(ValidationUtils.isValidIsoDate(""))
        assertFalse(ValidationUtils.isValidIsoDate("tidak-valid"))
    }

    @Test
    fun `konversi millis UTC dan ISO date bersifat simetris tanpa pergeseran hari`() {
        val millis = ValidationUtils.parseIsoDateToUtcMillis("2026-12-29")
        assertNotNull(millis)
        assertEquals("2026-12-29", ValidationUtils.formatUtcMillisToIsoDate(millis!!))
        assertNull(ValidationUtils.parseIsoDateToUtcMillis("2026-02-30"))
    }

    @Test
    fun `isValidEmail dan isValidPassword memvalidasi input autentikasi`() {
        assertTrue(ValidationUtils.isValidEmail("ilham@umy.ac.id"))
        assertFalse(ValidationUtils.isValidEmail("ilham.umy.ac.id"))
        assertFalse(ValidationUtils.isValidEmail(""))

        assertTrue(ValidationUtils.isValidPassword("123456"))
        assertFalse(ValidationUtils.isValidPassword("12345"))
        assertFalse(ValidationUtils.isValidPassword("   "))
    }

    @Test
    fun `parsePositiveInt dan calculateProgressRatio menangani angka batas dengan aman`() {
        assertEquals(25, ValidationUtils.parsePositiveInt(" 25 "))
        assertNull(ValidationUtils.parsePositiveInt("0"))
        assertNull(ValidationUtils.parsePositiveInt("-5"))
        assertNull(ValidationUtils.parsePositiveInt("abc"))

        assertEquals(0f, ValidationUtils.calculateProgressRatio(10, 0), 0.0001f)
        assertEquals(0.5f, ValidationUtils.calculateProgressRatio(15, 30), 0.0001f)
        assertEquals(1f, ValidationUtils.calculateProgressRatio(50, 30), 0.0001f)
        assertEquals(0f, ValidationUtils.calculateProgressRatio(-4, 30), 0.0001f)
    }
}

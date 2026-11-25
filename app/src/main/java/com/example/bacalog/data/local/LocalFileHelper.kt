package com.example.bacalog.data.local

import android.content.Context
import com.example.bacalog.data.model.BacaanDto
import com.example.bacalog.data.model.ProgresDto
import com.example.bacalog.data.model.TargetDto
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FileLaporanDto(
    val fileName: String,
    val fileSize: Long,
    val formattedSize: String,
    val lastModified: Long,
    val formattedDate: String
)

object LocalFileHelper {

    private const val FILE_PREFIX = "laporan_bacalog_"
    private const val FILE_EXTENSION = ".txt"

    /**
     * Menyimpan (Export) rekapitulasi data bacaan, progres, dan target ke file .txt di internal storage
     * menggunakan context.openFileOutput() sesuai Pedoman PAM Poin 9.
     */
    fun exportLaporanTxt(
        context: Context,
        namaPengguna: String,
        listBacaan: List<BacaanDto>,
        listProgres: List<ProgresDto>,
        listTarget: List<TargetDto>
    ): Result<String> {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val dateDisplay = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID")).format(Date())
            val fileName = "$FILE_PREFIX$timestamp$FILE_EXTENSION"

            val stringBuilder = StringBuilder()
            stringBuilder.appendLine("=====================================================================")
            stringBuilder.appendLine("          BACALOG — LAPORAN PROGRES BACAAN & BELAJAR MANDIRI         ")
            stringBuilder.appendLine("=====================================================================")
            stringBuilder.appendLine("Pengguna      : $namaPengguna (Ilham Saputra — NIM: 20240140118)")
            stringBuilder.appendLine("Waktu Ekspor  : $dateDisplay")
            stringBuilder.appendLine("Total Bahan   : ${listBacaan.size} Bahan Bacaan")
            stringBuilder.appendLine("Total Progres : ${listProgres.size} Sesi Belajar")
            stringBuilder.appendLine("Total Target  : ${listTarget.size} Target Periodik")
            stringBuilder.appendLine("=====================================================================")
            stringBuilder.appendLine()

            // 1. Rekapitulasi Bahan Bacaan
            stringBuilder.appendLine("---------------------------------------------------------------------")
            stringBuilder.appendLine("1. DAFTAR PUSTAKA & BAHAN BACAAN")
            stringBuilder.appendLine("---------------------------------------------------------------------")
            if (listBacaan.isEmpty()) {
                stringBuilder.appendLine("(Belum ada bahan bacaan yang tercatat)")
            } else {
                listBacaan.forEachIndexed { index, b ->
                    val nomor = index + 1
                    stringBuilder.appendLine("$nomor. ${b.judul}")
                    stringBuilder.appendLine("   - Kategori / Matkul : ${b.namaKategori.ifBlank { "-" }}")
                    stringBuilder.appendLine("   - Penulis / Sumber  : ${b.penulisSumber}")
                    stringBuilder.appendLine("   - Total Halaman     : ${b.totalHalaman} hal (Tercapai: ${b.halamanTerakhir} hal)")
                    stringBuilder.appendLine("   - Status Baca       : ${b.statusBaca}")
                    stringBuilder.appendLine()
                }
            }
            stringBuilder.appendLine()

            // 2. Rekapitulasi Target Baca Periodik
            stringBuilder.appendLine("---------------------------------------------------------------------")
            stringBuilder.appendLine("2. TARGET BACA PERIODIK")
            stringBuilder.appendLine("---------------------------------------------------------------------")
            if (listTarget.isEmpty()) {
                stringBuilder.appendLine("(Belum ada target baca periodik yang tercatat)")
            } else {
                listTarget.forEachIndexed { index, t ->
                    val nomor = index + 1
                    stringBuilder.appendLine("$nomor. ${t.judulBacaan ?: "Target ID #${t.id}"}")
                    stringBuilder.appendLine("   - Target Capaian    : ${t.targetHalaman} Halaman (Capaian: ${t.capaianTertinggi ?: 0} hal)")
                    stringBuilder.appendLine("   - Tenggat Waktu     : ${t.deadline}")
                    stringBuilder.appendLine("   - Status Target     : ${t.statusTarget}")
                    stringBuilder.appendLine()
                }
            }
            stringBuilder.appendLine()

            // 3. Rekapitulasi Riwayat Catatan Progres
            stringBuilder.appendLine("---------------------------------------------------------------------")
            stringBuilder.appendLine("3. RIWAYAT LOG CATATAN PROGRES BELAJAR")
            stringBuilder.appendLine("---------------------------------------------------------------------")
            if (listProgres.isEmpty()) {
                stringBuilder.appendLine("(Belum ada riwayat catatan progres belajar)")
            } else {
                listProgres.forEachIndexed { index, p ->
                    val nomor = index + 1
                    val judul = p.judulBacaan.ifBlank { "ID #${p.bacaanId}" }
                    stringBuilder.appendLine("$nomor. Tanggal: ${p.tanggalBaca} | Bahan: $judul")
                    stringBuilder.appendLine("   - Halaman Dicapai   : Hal. ${p.halamanDicapai}")
                    stringBuilder.appendLine("   - Target Sesi       : ${p.targetHarian} Halaman")
                    stringBuilder.appendLine("   - Ringkasan Materi  : ${p.ringkasanMateri}")
                    stringBuilder.appendLine()
                }
            }

            stringBuilder.appendLine("=====================================================================")
            stringBuilder.appendLine("      Dicatat dan disimpan secara mandiri melalui Aplikasi BacaLog   ")
            stringBuilder.appendLine("=====================================================================")

            // Simpan ke Internal Storage (openFileOutput)
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
                output.write(stringBuilder.toString().toByteArray(Charsets.UTF_8))
            }

            Result.success(fileName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Membaca daftar berkas .txt laporan yang tersimpan di internal storage (context.filesDir)
     */
    fun getDaftarFileLaporan(context: Context): List<FileLaporanDto> {
        val filesDir = context.filesDir ?: return emptyList()
        val files = filesDir.listFiles { _, name ->
            name.startsWith(FILE_PREFIX) && name.endsWith(FILE_EXTENSION)
        } ?: return emptyList()

        val dateFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))

        return files.sortedByDescending { it.lastModified() }.map { file ->
            val sizeBytes = file.length()
            val formattedSize = when {
                sizeBytes < 1024 -> "$sizeBytes B"
                sizeBytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", sizeBytes / 1024f)
                else -> String.format(Locale.US, "%.1f MB", sizeBytes / (1024f * 1024f))
            }
            FileLaporanDto(
                fileName = file.name,
                fileSize = sizeBytes,
                formattedSize = formattedSize,
                lastModified = file.lastModified(),
                formattedDate = dateFormatter.format(Date(file.lastModified()))
            )
        }
    }

    /**
     * Membaca isi berkas .txt laporan dari internal storage (context.openFileInput)
     */
    fun bacaFileLaporan(context: Context, fileName: String): Result<String> {
        return try {
            val content = context.openFileInput(fileName).bufferedReader(Charsets.UTF_8).use {
                it.readText()
            }
            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Menghapus berkas .txt laporan dari internal storage
     */
    fun hapusFileLaporan(context: Context, fileName: String): Boolean {
        return try {
            context.deleteFile(fileName)
        } catch (e: Exception) {
            false
        }
    }
}

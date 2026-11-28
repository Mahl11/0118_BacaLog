# BacaLog — Aplikasi Pencatat Progres Bacaan dan Belajar Mandiri

- **Nama :** Ilham Saputra
- **NIM :** 20240140118
- **Kelas :** D
- **Mata Kuliah :** Pengembangan Aplikasi Mobile (PAM) 2026
- **Format Repositori :** `0118_BacaLog`
- **Repositori Backend API :** `https://github.com/Mahl11/0118_API_BacaLog.git`

---

## Ringkasan Proyek

**BacaLog** adalah aplikasi Android berbasis Kotlin Jetpack Compose (Material 3) yang dirancang untuk membantu mahasiswa dan pembelajar mandiri mencatat bahan bacaan (buku referensi kuliah, jurnal, dan modul belajar), memantau log progres harian (halaman buku yang dicapai dan ringkasan refleksi materi), serta menetapkan target baca periodik secara terukur.

Aplikasi ini menggunakan arsitektur **MVVM (Model-View-ViewModel)** dengan **Navigation Compose**, terhubung ke database **MySQL** melalui **REST API (JSON)** dengan autentikasi **JWT (Access & Refresh Token)**, mengelola **6 entitas utama berelasi**, menyediakan **5 modul fitur CRUDS penuh**, serta mendukung pencadangan dan pembacaan laporan belajar ke file lokal (`.txt`).

---

## Status Milestone & Fitur Mingguan

| No | Modul / Fitur | Status Pengerjaan | Target Pertemuan |
|:---:|---|:---:|:---:|
| 1 | Inisialisasi Project Compose & Arsitektur MVVM | Selesai | Pertemuan 9 |
| 2 | Setup Tema Material 3 & Zero Hardcode Resources | Selesai | Pertemuan 9 |
| 3 | Autentikasi JWT (Login, Register, Refresh Token) | Selesai | Pertemuan 10 |
| 4 | Modul CRUDS 1 — Kategori Topik Belajar | Selesai | Pertemuan 11 |
| 5 | Modul CRUDS 2 — Bahan Bacaan (Filter Mata Kuliah) | Selesai | Pertemuan 11 |
| 6 | Modul CRUDS 3 — Catatan Progres Belajar | Selesai | Pertemuan 12 |
| 7 | Modul CRUDS 4 — Target Baca Periodik | Selesai | Pertemuan 12 |
| 8 | Modul CRUDS 5 — Profil & Catatan Fokus Belajar | Selesai | Pertemuan 13 |
| 9 | Simpan & Baca Laporan File Lokal (.txt) | Selesai | Pertemuan 14 |
| 10 | Pengujian Menyeluruh & Finalisasi Responsi | Siap Diuji | Pertemuan 15 (UAS) |

---

## Progress Mingguan (Pertemuan 9 – Pertemuan 16)

| Pertemuan | Tanggal | Ringkasan Progres | Commit Hash |
|---|---|---|---|
| **Pertemuan 9** | 22–28 November 2026 | Inisialisasi proyek Compose & MVVM, autentikasi JWT (Login, Register, Auto-Refresh + thread-safe lock), navigasi Compose + Beranda (`PullToRefreshBox`), 5 modul CRUDS penuh, `DatePickerDialog` Material 3, penyimpanan berkas lokal `.txt`, unit test JUnit 4, serta penyelarasan UI/UX & pemisahan repo API (`0118_API_BacaLog`). | `e713bb2`, `f3d563c`, `a65e88f`, `aa858fa`, `4aaf4ba`, `d666271`, `910a5b8`, `03b7ef3`, `81743e2`, `24b82b4`, `7db096d`, `d2354cd`, `6021ab0`, `ea3b0b8` |
| **Pertemuan 10** | Desember 2026 | *Akan diperbarui sesuai progres mingguan bulan Desember.* | - |
| **Pertemuan 11** | Desember 2026 | *Akan diperbarui sesuai progres mingguan bulan Desember.* | - |
| **Pertemuan 12** | Desember 2026 | *Akan diperbarui sesuai progres mingguan bulan Desember.* | - |
| **Pertemuan 13** | Desember 2026 | *Akan diperbarui sesuai progres mingguan bulan Desember.* | - |
| **Pertemuan 14** | Desember 2026 | *Akan diperbarui sesuai progres mingguan bulan Desember.* | - |
| **Pertemuan 15** | Desember 2026 | *Akan diperbarui sesuai progres mingguan bulan Desember.* | - |
| **Pertemuan 16** | Desember 2026 | *Akan diperbarui sesuai progres mingguan bulan Desember.* | - |

---

## Panduan Menjalankan Aplikasi

1. **Jalankan Basis Data & Backend Server (`0118_API_BacaLog`):**
   ```bash
   # Clone atau buka repositori backend API terpisah
   git clone https://github.com/Mahl11/0118_API_BacaLog.git
   cd 0118_API_BacaLog
   npm install
   npm start
   ```
   *(Atau jalankan `JALANKAN_BACKEND.bat` untuk pengecekan otomatis MySQL port `3306` dan server Express port `5000`)*
   *Base URL API Android Emulator:* `http://10.0.2.2:5000/api/`

2. **Jalankan Proyek Android Studio:**
   - Buka folder proyek di Android Studio.
   - Sinkronisasi Gradle dan jalankan pada Android Emulator atau perangkat fisik.

---

## Preview Tampilan

*(Screenshot antarmuka aplikasi running di emulator berdampingan dengan Android Studio akan diperbarui secara berkala seiring berjalannya milestone fitur)*

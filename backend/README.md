# Backend REST API BacaLog (PAM 2026)

Layanan backend RESTful API untuk aplikasi Android **BacaLog (0118_BacaLog)** yang dibangun menggunakan Node.js, Express, dan basis data MySQL.

## Identitas Pengembang
- **Nama :** Ilham Saputra
- **NIM  :** 20240140118
- **Kelas:** D
- **Mata Kuliah:** Pengembangan Aplikasi Mobile (PAM) 2026

---

## Prasyarat
1. Node.js (v18 atau lebih baru)
2. MySQL Server (bisa melalui XAMPP atau MySQL Community Server)

---

## Langkah Menjalankan Backend Lokal

1. **Import Database:**
   - Buka phpMyAdmin (`http://localhost/phpmyadmin`) atau terminal MySQL.
   - Import atau jalankan query dari file `backend/database.sql`.
   - Basis data `bacalog_db` beserta seluruh **6 tabel relasional** (`users`, `kategori_topik`, `bahan_bacaan`, `catatan_progres`, `target_baca`, dan `catatan_fokus`) serta data seeder awal akan otomatis dibuat. Skema juga diverifikasi ulang secara otomatis saat server backend dijalankan.

2. **Install Dependensi:**
   ```bash
   cd backend
   npm install
   ```

3. **Jalankan Server:**
   ```bash
   npm start
   ```
   Atau di Windows, jalankan langsung skrip `backend/JALANKAN_BACKEND.bat` (klik dua kali, atau lewat shortcut Desktop) yang otomatis memeriksa/menyalakan MySQL XAMPP (port `3306`) sekaligus server REST API (port `5000`) sebelum demo aplikasi.
   Server akan aktif di:
   - Akses Browser / Postman: `http://localhost:5000/api/`
   - Akses dari Android Emulator: `http://10.0.2.2:5000/api/`

---

## Daftar Endpoint Autentikasi
- `POST /api/auth/register` - Mendaftarkan akun user baru (mengembalikan JWT access & refresh token).
- `POST /api/auth/login` - Masuk ke sistem (mengembalikan JWT access & refresh token).
- `POST /api/auth/refresh` - Memperbarui token akses saat expired menggunakan refresh token.
- `POST /api/auth/logout` - Menghapus sesi refresh token di server.
- `GET /api/profil` - Mengambil profil user aktif (memerlukan header `Authorization: Bearer <access_token>`).

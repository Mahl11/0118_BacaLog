const express = require('express');
const cors = require('cors');
const mysql = require('mysql2/promise');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcrypt');

const app = express();
const PORT = process.env.PORT || 5000;
const JWT_SECRET = process.env.JWT_SECRET || 'bacalog_super_secret_jwt_key_2026';
const JWT_REFRESH_SECRET = process.env.JWT_REFRESH_SECRET || 'bacalog_super_refresh_secret_key_2026';

app.use(cors());
app.use(express.json());

// Konfigurasi MySQL Connection Pool
const dbPool = mysql.createPool({
    host: process.env.DB_HOST || 'localhost',
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || '',
    database: process.env.DB_NAME || 'bacalog_db',
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0
});

// Middleware Verifikasi JWT Token
const authenticateToken = (req, res, next) => {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];

    if (!token) {
        return res.status(401).json({ status: 'error', message: 'Token akses tidak ditemukan' });
    }

    jwt.verify(token, JWT_SECRET, (err, user) => {
        if (err) {
            return res.status(401).json({ status: 'error', message: 'Token tidak valid atau kadaluarsa' });
        }
        req.user = user;
        next();
    });
};

// ==================== ENDPOINT AUTENTIKASI ====================

// 1. Registrasi Pengguna Baru
app.post('/api/auth/register', async (req, res) => {
    try {
        const { nama, email, password } = req.body;

        if (!nama || !email || !password) {
            return res.status(400).json({ status: 'error', message: 'Semua kolom wajib diisi' });
        }

        const [existing] = await dbPool.query('SELECT id FROM users WHERE email = ?', [email]);
        if (existing.length > 0) {
            return res.status(409).json({ status: 'error', message: 'Email sudah terdaftar' });
        }

        const hashedPassword = await bcrypt.hash(password, 10);
        const [result] = await dbPool.query(
            'INSERT INTO users (nama, email, password) VALUES (?, ?, ?)',
            [nama, email, hashedPassword]
        );

        const userId = result.insertId;
        const accessToken = jwt.sign({ id: userId, email }, JWT_SECRET, { expiresIn: '1h' });
        const refreshToken = jwt.sign({ id: userId, email }, JWT_REFRESH_SECRET, { expiresIn: '7d' });

        await dbPool.query('UPDATE users SET refresh_token = ? WHERE id = ?', [refreshToken, userId]);

        res.status(201).json({
            status: 'success',
            message: 'Registrasi berhasil',
            access_token: accessToken,
            refresh_token: refreshToken,
            user: { id: userId, nama, email, bio_belajar: 'Semangat belajar dan membaca!' }
        });
    } catch (error) {
        res.status(500).json({ status: 'error', message: error.message });
    }
});

// 2. Login Pengguna
app.post('/api/auth/login', async (req, res) => {
    try {
        const { email, password } = req.body;

        if (!email || !password) {
            return res.status(400).json({ status: 'error', message: 'Email dan kata sandi wajib diisi' });
        }

        const [users] = await dbPool.query('SELECT * FROM users WHERE email = ?', [email]);
        if (users.length === 0) {
            return res.status(401).json({ status: 'error', message: 'Email atau kata sandi salah' });
        }

        const user = users[0];
        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) {
            return res.status(401).json({ status: 'error', message: 'Email atau kata sandi salah' });
        }

        const accessToken = jwt.sign({ id: user.id, email: user.email }, JWT_SECRET, { expiresIn: '1h' });
        const refreshToken = jwt.sign({ id: user.id, email: user.email }, JWT_REFRESH_SECRET, { expiresIn: '7d' });

        await dbPool.query('UPDATE users SET refresh_token = ? WHERE id = ?', [refreshToken, user.id]);

        res.json({
            status: 'success',
            message: 'Login berhasil',
            access_token: accessToken,
            refresh_token: refreshToken,
            user: { id: user.id, nama: user.nama, email: user.email, bio_belajar: user.bio_belajar }
        });
    } catch (error) {
        res.status(500).json({ status: 'error', message: error.message });
    }
});

// 3. Refresh Access Token
app.post('/api/auth/refresh', async (req, res) => {
    try {
        const { refresh_token } = req.body;
        if (!refresh_token) {
            return res.status(400).json({ status: 'error', message: 'Refresh token wajib disertakan' });
        }

        jwt.verify(refresh_token, JWT_REFRESH_SECRET, async (err, decoded) => {
            if (err) {
                return res.status(403).json({ status: 'error', message: 'Refresh token tidak valid' });
            }

            const [users] = await dbPool.query('SELECT * FROM users WHERE id = ? AND refresh_token = ?', [decoded.id, refresh_token]);
            if (users.length === 0) {
                return res.status(403).json({ status: 'error', message: 'Sesi login telah berakhir' });
            }

            const newAccessToken = jwt.sign({ id: decoded.id, email: decoded.email }, JWT_SECRET, { expiresIn: '1h' });
            res.json({
                status: 'success',
                access_token: newAccessToken
            });
        });
    } catch (error) {
        res.status(500).json({ status: 'error', message: error.message });
    }
});

// 4. Logout Pengguna
app.post('/api/auth/logout', authenticateToken, async (req, res) => {
    try {
        await dbPool.query('UPDATE users SET refresh_token = NULL WHERE id = ?', [req.user.id]);
        res.json({ status: 'success', message: 'Berhasil keluar dari aplikasi' });
    } catch (error) {
        res.status(500).json({ status: 'error', message: error.message });
    }
});

// 5. Profil Pengguna
app.get('/api/profil', authenticateToken, async (req, res) => {
    try {
        const [users] = await dbPool.query('SELECT id, nama, email, bio_belajar FROM users WHERE id = ?', [req.user.id]);
        if (users.length === 0) {
            return res.status(404).json({ status: 'error', message: 'Pengguna tidak ditemukan' });
        }
        res.json(users[0]);
    } catch (error) {
        res.status(500).json({ status: 'error', message: error.message });
    }
});

// 6. Ringkasan Statistik Dashboard
app.get('/api/dashboard/statistik', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.id;
        const [bacaanRows] = await dbPool.query(
            'SELECT COUNT(*) AS total, SUM(CASE WHEN status_baca = "Sedang Dibaca" THEN 1 ELSE 0 END) AS sedang, SUM(CASE WHEN status_baca = "Selesai" THEN 1 ELSE 0 END) AS selesai FROM bahan_bacaan WHERE user_id = ?',
            [userId]
        );
        const [progresRows] = await dbPool.query(
            'SELECT COALESCE(SUM(halaman_dicapai), 0) AS total_halaman FROM catatan_progres WHERE user_id = ?',
            [userId]
        );

        res.json({
            status: 'success',
            total_bacaan: bacaanRows[0].total || 0,
            sedang_dibaca: bacaanRows[0].sedang || 0,
            selesai_dibaca: bacaanRows[0].selesai || 0,
            total_halaman: progresRows[0].total_halaman || 0
        });
    } catch (error) {
        res.status(500).json({ status: 'error', message: error.message });
    }
});

// ==================== ENDPOINT CRUDS 1: KATEGORI TOPIK ====================

// 7. Ambil Daftar & Pencarian Kategori Topik (Read & Search)
app.get('/api/kategori', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.id;
        const search = req.query.search || '';

        let sql = `
            SELECT k.id, k.user_id, k.nama_topik, k.deskripsi, k.created_at,
                   COUNT(b.id) AS total_bacaan
            FROM kategori_topik k
            LEFT JOIN bahan_bacaan b ON b.kategori_id = k.id
            WHERE k.user_id = ?
        `;
        const params = [userId];

        if (search.trim() !== '') {
            sql += ' AND k.nama_topik LIKE ?';
            params.push(`%${search.trim()}%`);
        }

        sql += ' GROUP BY k.id ORDER BY k.id DESC';

        const [rows] = await dbPool.query(sql, params);
        res.json({
            status: 'success',
            data: rows
        });
    } catch (error) {
        res.status(500).json({ status: 'error', message: error.message });
    }
});

// 8. Tambah Kategori Topik Baru (Create)
app.post('/api/kategori', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.id;
        const { nama_topik, deskripsi } = req.body;

        if (!nama_topik || nama_topik.trim() === '') {
            return res.status(400).json({ status: 'error', message: 'Nama kategori topik wajib diisi' });
        }

        const [result] = await dbPool.query(
            'INSERT INTO kategori_topik (user_id, nama_topik, deskripsi) VALUES (?, ?, ?)',
            [userId, nama_topik.trim(), deskripsi ? deskripsi.trim() : null]
        );

        res.status(201).json({
            status: 'success',
            message: 'Kategori topik berhasil ditambahkan',
            id: result.insertId
        });
    } catch (error) {
        res.status(500).json({ status: 'error', message: error.message });
    }
});

// 9. Perbarui Kategori Topik (Update)
app.put('/api/kategori/:id', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.id;
        const kategoriId = req.params.id;
        const { nama_topik, deskripsi } = req.body;

        if (!nama_topik || nama_topik.trim() === '') {
            return res.status(400).json({ status: 'error', message: 'Nama kategori topik wajib diisi' });
        }

        const [result] = await dbPool.query(
            'UPDATE kategori_topik SET nama_topik = ?, deskripsi = ? WHERE id = ? AND user_id = ?',
            [nama_topik.trim(), deskripsi ? deskripsi.trim() : null, kategoriId, userId]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({ status: 'error', message: 'Kategori tidak ditemukan atau bukan milik Anda' });
        }

        res.json({
            status: 'success',
            message: 'Kategori topik berhasil diperbarui'
        });
    } catch (error) {
        res.status(500).json({ status: 'error', message: error.message });
    }
});

// 10. Hapus Kategori Topik (Delete)
app.delete('/api/kategori/:id', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.id;
        const kategoriId = req.params.id;

        const [result] = await dbPool.query(
            'DELETE FROM kategori_topik WHERE id = ? AND user_id = ?',
            [kategoriId, userId]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({ status: 'error', message: 'Kategori tidak ditemukan atau bukan milik Anda' });
        }

        res.json({
            status: 'success',
            message: 'Kategori topik berhasil dihapus'
        });
    } catch (error) {
        res.status(500).json({ status: 'error', message: error.message });
    }
});

// ==================== ENDPOINT CRUDS 2: BAHAN BACAAN ====================

// 11. Ambil Daftar & Pencarian Bahan Bacaan (Read & Search)
app.get('/api/bacaan', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.id;
        const search = req.query.search || '';
        const kategoriId = req.query.kategori_id || '';
        const statusBaca = req.query.status_baca || '';

        let sql = `
            SELECT b.id, b.user_id, b.kategori_id, b.judul, b.penulis_sumber,
                   b.total_halaman, b.status_baca, b.created_at,
                   k.nama_topik AS nama_kategori,
                   COALESCE(MAX(cp.halaman_dicapai), 0) AS halaman_terakhir
            FROM bahan_bacaan b
            JOIN kategori_topik k ON b.kategori_id = k.id
            LEFT JOIN catatan_progres cp ON cp.bacaan_id = b.id
            WHERE b.user_id = ?
        `;
        const params = [userId];

        if (search.trim() !== '') {
            sql += ' AND (b.judul LIKE ? OR b.penulis_sumber LIKE ?)';
            const term = `%${search.trim()}%`;
            params.push(term, term);
        }

        if (kategoriId.trim() !== '') {
            sql += ' AND b.kategori_id = ?';
            params.push(kategoriId.trim());
        }

        if (statusBaca.trim() !== '' && statusBaca !== 'Semua') {
            sql += ' AND b.status_baca = ?';
            params.push(statusBaca.trim());
        }

        sql += ' GROUP BY b.id ORDER BY b.id DESC';

        const [rows] = await dbPool.query(sql, params);
        res.json({
            status: 'success',
            data: rows
        });
    } catch (error) {
        res.status(500).json({ status: 'error', message: error.message });
    }
});

// 12. Tambah Bahan Bacaan Baru (Create)
app.post('/api/bacaan', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.id;
        const { kategori_id, judul, penulis_sumber, total_halaman, status_baca } = req.body;

        if (!kategori_id || !judul || !penulis_sumber || !total_halaman) {
            return res.status(400).json({
                status: 'error',
                message: 'Kategori, judul, penulis/sumber, dan total halaman wajib diisi'
            });
        }

        const hal = parseInt(total_halaman, 10);
        if (isNaN(hal) || hal <= 0) {
            return res.status(400).json({
                status: 'error',
                message: 'Total halaman harus berupa angka positif'
            });
        }

        // Validasi kepemilikan kategori
        const [katCheck] = await dbPool.query(
            'SELECT id FROM kategori_topik WHERE id = ? AND user_id = ?',
            [kategori_id, userId]
        );
        if (katCheck.length === 0) {
            return res.status(400).json({
                status: 'error',
                message: 'Kategori topik yang dipilih tidak valid'
            });
        }

        const statusValid = ['Belum Dibaca', 'Sedang Dibaca', 'Selesai'];
        const statusFinal = statusValid.includes(status_baca) ? status_baca : 'Belum Dibaca';

        const [result] = await dbPool.query(
            'INSERT INTO bahan_bacaan (user_id, kategori_id, judul, penulis_sumber, total_halaman, status_baca) VALUES (?, ?, ?, ?, ?, ?)',
            [userId, kategori_id, judul.trim(), penulis_sumber.trim(), hal, statusFinal]
        );

        res.status(201).json({
            status: 'success',
            message: 'Bahan bacaan berhasil ditambahkan',
            id: result.insertId
        });
    } catch (error) {
        res.status(500).json({ status: 'error', message: error.message });
    }
});

// 13. Perbarui Bahan Bacaan (Update)
app.put('/api/bacaan/:id', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.id;
        const bacaanId = req.params.id;
        const { kategori_id, judul, penulis_sumber, total_halaman, status_baca } = req.body;

        if (!kategori_id || !judul || !penulis_sumber || !total_halaman) {
            return res.status(400).json({
                status: 'error',
                message: 'Kategori, judul, penulis/sumber, dan total halaman wajib diisi'
            });
        }

        const hal = parseInt(total_halaman, 10);
        if (isNaN(hal) || hal <= 0) {
            return res.status(400).json({
                status: 'error',
                message: 'Total halaman harus berupa angka positif'
            });
        }

        const statusValid = ['Belum Dibaca', 'Sedang Dibaca', 'Selesai'];
        const statusFinal = statusValid.includes(status_baca) ? status_baca : 'Belum Dibaca';

        const [result] = await dbPool.query(
            'UPDATE bahan_bacaan SET kategori_id = ?, judul = ?, penulis_sumber = ?, total_halaman = ?, status_baca = ? WHERE id = ? AND user_id = ?',
            [kategori_id, judul.trim(), penulis_sumber.trim(), hal, statusFinal, bacaanId, userId]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({
                status: 'error',
                message: 'Bahan bacaan tidak ditemukan atau bukan milik Anda'
            });
        }

        res.json({
            status: 'success',
            message: 'Bahan bacaan berhasil diperbarui'
        });
    } catch (error) {
        res.status(500).json({ status: 'error', message: error.message });
    }
});

// 14. Hapus Bahan Bacaan (Delete)
app.delete('/api/bacaan/:id', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.id;
        const bacaanId = req.params.id;

        const [result] = await dbPool.query(
            'DELETE FROM bahan_bacaan WHERE id = ? AND user_id = ?',
            [bacaanId, userId]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({
                status: 'error',
                message: 'Bahan bacaan tidak ditemukan atau bukan milik Anda'
            });
        }

        res.json({
            status: 'success',
            message: 'Bahan bacaan berhasil dihapus'
        });
    } catch (error) {
        res.status(500).json({ status: 'error', message: error.message });
    }
});

// ==================== ENDPOINT CRUDS 3: CATATAN PROGRES ====================

// 15. Ambil Riwayat & Pencarian Catatan Progres (Read & Search)
app.get('/api/progres', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.id;
        const search = req.query.search || '';
        const bacaanId = req.query.bacaan_id || '';

        let sql = `
            SELECT cp.id, cp.user_id, cp.bacaan_id, cp.tanggal_baca,
                   cp.halaman_dicapai, cp.ringkasan_materi, cp.target_harian, cp.created_at,
                   b.judul AS judul_bacaan, b.total_halaman,
                   k.nama_topik AS nama_kategori
            FROM catatan_progres cp
            JOIN bahan_bacaan b ON cp.bacaan_id = b.id
            JOIN kategori_topik k ON b.kategori_id = k.id
            WHERE cp.user_id = ?
        `;
        const params = [userId];

        if (search.trim() !== '') {
            sql += ' AND (cp.ringkasan_materi LIKE ? OR cp.tanggal_baca LIKE ? OR b.judul LIKE ?)';
            const term = `%${search.trim()}%`;
            params.push(term, term, term);
        }

        if (bacaanId.trim() !== '') {
            sql += ' AND cp.bacaan_id = ?';
            params.push(bacaanId.trim());
        }

        sql += ' ORDER BY cp.id DESC';

        const [rows] = await dbPool.query(sql, params);
        res.json({
            status: 'success',
            data: rows
        });
    } catch (error) {
        res.status(500).json({ status: 'error', message: error.message });
    }
});

// 16. Tambah Catatan Progres Baru (Create)
app.post('/api/progres', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.id;
        const { bacaan_id, tanggal_baca, halaman_dicapai, ringkasan_materi, target_harian } = req.body;

        if (!bacaan_id || !tanggal_baca || halaman_dicapai === undefined || !ringkasan_materi) {
            return res.status(400).json({
                status: 'error',
                message: 'Bahan bacaan, tanggal baca, halaman dicapai, dan ringkasan materi wajib diisi'
            });
        }

        const hal = parseInt(halaman_dicapai, 10);
        if (isNaN(hal) || hal < 0) {
            return res.status(400).json({
                status: 'error',
                message: 'Halaman dicapai harus berupa angka positif atau nol'
            });
        }

        const target = target_harian ? parseInt(target_harian, 10) : hal;

        // Validasi kepemilikan bahan bacaan
        const [bacaanCheck] = await dbPool.query(
            'SELECT id, total_halaman, status_baca FROM bahan_bacaan WHERE id = ? AND user_id = ?',
            [bacaan_id, userId]
        );
        if (bacaanCheck.length === 0) {
            return res.status(400).json({
                status: 'error',
                message: 'Bahan bacaan yang dipilih tidak valid'
            });
        }

        const [result] = await dbPool.query(
            'INSERT INTO catatan_progres (user_id, bacaan_id, tanggal_baca, halaman_dicapai, ringkasan_materi, target_harian) VALUES (?, ?, ?, ?, ?, ?)',
            [userId, bacaan_id, tanggal_baca.trim(), hal, ringkasan_materi.trim(), isNaN(target) ? 0 : target]
        );

        // Otomatis update status bahan bacaan jika halaman dicapai >= total halaman
        const bacaanItem = bacaanCheck[0];
        if (hal >= bacaanItem.total_halaman && bacaanItem.status_baca !== 'Selesai') {
            await dbPool.query('UPDATE bahan_bacaan SET status_baca = "Selesai" WHERE id = ?', [bacaan_id]);
        } else if (hal > 0 && bacaanItem.status_baca === 'Belum Dibaca') {
            await dbPool.query('UPDATE bahan_bacaan SET status_baca = "Sedang Dibaca" WHERE id = ?', [bacaan_id]);
        }

        res.status(201).json({
            status: 'success',
            message: 'Catatan progres berhasil ditambahkan',
            id: result.insertId
        });
    } catch (error) {
        res.status(500).json({ status: 'error', message: error.message });
    }
});

// 17. Perbarui Catatan Progres (Update)
app.put('/api/progres/:id', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.id;
        const progresId = req.params.id;
        const { bacaan_id, tanggal_baca, halaman_dicapai, ringkasan_materi, target_harian } = req.body;

        if (!bacaan_id || !tanggal_baca || halaman_dicapai === undefined || !ringkasan_materi) {
            return res.status(400).json({
                status: 'error',
                message: 'Bahan bacaan, tanggal baca, halaman dicapai, dan ringkasan materi wajib diisi'
            });
        }

        const hal = parseInt(halaman_dicapai, 10);
        if (isNaN(hal) || hal < 0) {
            return res.status(400).json({
                status: 'error',
                message: 'Halaman dicapai harus berupa angka positif atau nol'
            });
        }

        const target = target_harian ? parseInt(target_harian, 10) : hal;

        const [result] = await dbPool.query(
            'UPDATE catatan_progres SET bacaan_id = ?, tanggal_baca = ?, halaman_dicapai = ?, ringkasan_materi = ?, target_harian = ? WHERE id = ? AND user_id = ?',
            [bacaan_id, tanggal_baca.trim(), hal, ringkasan_materi.trim(), isNaN(target) ? 0 : target, progresId, userId]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({
                status: 'error',
                message: 'Catatan progres tidak ditemukan atau bukan milik Anda'
            });
        }

        res.json({
            status: 'success',
            message: 'Catatan progres berhasil diperbarui'
        });
    } catch (error) {
        res.status(500).json({ status: 'error', message: error.message });
    }
});

// 18. Hapus Catatan Progres (Delete)
app.delete('/api/progres/:id', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.id;
        const progresId = req.params.id;

        const [result] = await dbPool.query(
            'DELETE FROM catatan_progres WHERE id = ? AND user_id = ?',
            [progresId, userId]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({
                status: 'error',
                message: 'Catatan progres tidak ditemukan atau bukan milik Anda'
            });
        }

        res.json({
            status: 'success',
            message: 'Catatan progres berhasil dihapus'
        });
    } catch (error) {
        res.status(500).json({ status: 'error', message: error.message });
    }
});

// Jalankan Server
app.listen(PORT, '0.0.0.0', () => {
    console.log(`[BacaLog API] Server aktif di port ${PORT}`);
    console.log(`[BacaLog API] Android Emulator Base URL: http://10.0.2.2:${PORT}/api/`);
});

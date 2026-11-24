const express = require("express");
const cors = require("cors");
const mysql = require("mysql2/promise");
const jwt = require("jsonwebtoken");
const bcrypt = require("bcrypt");

const app = express();
const PORT = process.env.PORT || 5000;
const JWT_SECRET =
  process.env.JWT_SECRET || "bacalog_super_secret_jwt_key_2026";
const JWT_REFRESH_SECRET =
  process.env.JWT_REFRESH_SECRET || "bacalog_super_refresh_secret_key_2026";

app.use(cors());
app.use(express.json());

// Konfigurasi MySQL Connection Pool
const dbPool = mysql.createPool({
  host: process.env.DB_HOST || "localhost",
  user: process.env.DB_USER || "root",
  password: process.env.DB_PASSWORD || "",
  database: process.env.DB_NAME || "bacalog_db",
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0,
});

// Middleware Verifikasi JWT Token
const authenticateToken = (req, res, next) => {
  const authHeader = req.headers["authorization"];
  const token = authHeader && authHeader.split(" ")[1];

  if (!token) {
    return res
      .status(401)
      .json({ status: "error", message: "Token akses tidak ditemukan" });
  }

  jwt.verify(token, JWT_SECRET, (err, user) => {
    if (err) {
      return res.status(401).json({
        status: "error",
        message: "Token tidak valid atau kadaluarsa",
      });
    }
    req.user = user;
    next();
  });
};

// ==================== ENDPOINT AUTENTIKASI ====================

// 1. Registrasi Pengguna Baru
app.post("/api/auth/register", async (req, res) => {
  try {
    const { nama, email, password } = req.body;

    if (!nama || !email || !password) {
      return res
        .status(400)
        .json({ status: "error", message: "Semua kolom wajib diisi" });
    }

    const [existing] = await dbPool.query(
      "SELECT id FROM users WHERE email = ?",
      [email],
    );
    if (existing.length > 0) {
      return res
        .status(409)
        .json({ status: "error", message: "Email sudah terdaftar" });
    }

    const hashedPassword = await bcrypt.hash(password, 10);
    const [result] = await dbPool.query(
      "INSERT INTO users (nama, email, password) VALUES (?, ?, ?)",
      [nama, email, hashedPassword],
    );

    const userId = result.insertId;
    const accessToken = jwt.sign({ id: userId, email }, JWT_SECRET, {
      expiresIn: "1h",
    });
    const refreshToken = jwt.sign({ id: userId, email }, JWT_REFRESH_SECRET, {
      expiresIn: "7d",
    });

    await dbPool.query("UPDATE users SET refresh_token = ? WHERE id = ?", [
      refreshToken,
      userId,
    ]);

    res.status(201).json({
      status: "success",
      message: "Registrasi berhasil",
      access_token: accessToken,
      refresh_token: refreshToken,
      user: {
        id: userId,
        nama,
        email,
        bio_belajar: "Semangat belajar dan membaca!",
      },
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// 2. Login Pengguna
app.post("/api/auth/login", async (req, res) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res
        .status(400)
        .json({ status: "error", message: "Email dan kata sandi wajib diisi" });
    }

    const [users] = await dbPool.query("SELECT * FROM users WHERE email = ?", [
      email,
    ]);
    if (users.length === 0) {
      return res
        .status(401)
        .json({ status: "error", message: "Email atau kata sandi salah" });
    }

    const user = users[0];
    const isMatch = await bcrypt.compare(password, user.password);
    if (!isMatch) {
      return res
        .status(401)
        .json({ status: "error", message: "Email atau kata sandi salah" });
    }

    const accessToken = jwt.sign(
      { id: user.id, email: user.email },
      JWT_SECRET,
      { expiresIn: "1h" },
    );
    const refreshToken = jwt.sign(
      { id: user.id, email: user.email },
      JWT_REFRESH_SECRET,
      { expiresIn: "7d" },
    );

    await dbPool.query("UPDATE users SET refresh_token = ? WHERE id = ?", [
      refreshToken,
      user.id,
    ]);

    res.json({
      status: "success",
      message: "Login berhasil",
      access_token: accessToken,
      refresh_token: refreshToken,
      user: {
        id: user.id,
        nama: user.nama,
        email: user.email,
        bio_belajar: user.bio_belajar,
      },
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// 3. Refresh Access Token
app.post("/api/auth/refresh", async (req, res) => {
  try {
    const { refresh_token } = req.body;
    if (!refresh_token) {
      return res
        .status(400)
        .json({ status: "error", message: "Refresh token wajib disertakan" });
    }

    jwt.verify(refresh_token, JWT_REFRESH_SECRET, async (err, decoded) => {
      if (err) {
        return res
          .status(403)
          .json({ status: "error", message: "Refresh token tidak valid" });
      }

      const [users] = await dbPool.query(
        "SELECT * FROM users WHERE id = ? AND refresh_token = ?",
        [decoded.id, refresh_token],
      );
      if (users.length === 0) {
        return res
          .status(403)
          .json({ status: "error", message: "Sesi login telah berakhir" });
      }

      const newAccessToken = jwt.sign(
        { id: decoded.id, email: decoded.email },
        JWT_SECRET,
        { expiresIn: "1h" },
      );
      res.json({
        status: "success",
        access_token: newAccessToken,
      });
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// 4. Logout Pengguna
app.post("/api/auth/logout", authenticateToken, async (req, res) => {
  try {
    await dbPool.query("UPDATE users SET refresh_token = NULL WHERE id = ?", [
      req.user.id,
    ]);
    res.json({ status: "success", message: "Berhasil keluar dari aplikasi" });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// 5. Profil Pengguna (Read & Search)
app.get("/api/profil", authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const search = req.query.search || "";

    let sql =
      "SELECT id, nama, email, bio_belajar, catatan_fokus, created_at FROM users WHERE id = ?";
    const params = [userId];

    if (search.trim() !== "") {
      sql += " AND (nama LIKE ? OR bio_belajar LIKE ? OR catatan_fokus LIKE ?)";
      const q = `%${search.trim()}%`;
      params.push(q, q, q);
    }

    const [users] = await dbPool.query(sql, params);
    if (users.length === 0) {
      return res
        .status(404)
        .json({ status: "error", message: "Pengguna tidak ditemukan" });
    }

    res.json({
      status: "success",
      data: users[0],
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// 5b. Perbarui Profil Pengguna (Update Profil)
app.put("/api/profil", authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const { nama, bio_belajar, catatan_fokus, password_lama, password_baru } =
      req.body;

    if (!nama || nama.trim().isBlank?.() || nama.trim() === "") {
      return res
        .status(400)
        .json({ status: "error", message: "Nama lengkap tidak boleh kosong" });
    }

    // Jika ingin ganti password
    if (password_baru && password_baru.trim() !== "") {
      if (!password_lama) {
        return res.status(400).json({
          status: "error",
          message: "Kata sandi lama wajib diisi untuk verifikasi",
        });
      }
      const [users] = await dbPool.query(
        "SELECT password FROM users WHERE id = ?",
        [userId],
      );
      const isMatch = await bcrypt.compare(password_lama, users[0].password);
      if (!isMatch) {
        return res
          .status(401)
          .json({ status: "error", message: "Kata sandi lama salah" });
      }
      const hashed = await bcrypt.hash(password_baru.trim(), 10);
      await dbPool.query(
        "UPDATE users SET nama = ?, bio_belajar = ?, catatan_fokus = ?, password = ? WHERE id = ?",
        [
          nama.trim(),
          bio_belajar ? bio_belajar.trim() : null,
          catatan_fokus ? catatan_fokus.trim() : null,
          hashed,
          userId,
        ],
      );
    } else {
      await dbPool.query(
        "UPDATE users SET nama = ?, bio_belajar = ?, catatan_fokus = ? WHERE id = ?",
        [
          nama.trim(),
          bio_belajar ? bio_belajar.trim() : null,
          catatan_fokus ? catatan_fokus.trim() : null,
          userId,
        ],
      );
    }

    const [updated] = await dbPool.query(
      "SELECT id, nama, email, bio_belajar, catatan_fokus FROM users WHERE id = ?",
      [userId],
    );
    res.json({
      status: "success",
      message: "Profil pengguna berhasil diperbarui",
      data: updated[0],
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// 5c. Ambil Daftar & Pencarian Catatan Fokus Belajar (Read & Search)
app.get("/api/profil/fokus", authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const search = req.query.search || "";

    let sql =
      "SELECT id, user_id, fokus_materi, keterangan, target_selesai, status_fokus, created_at FROM catatan_fokus WHERE user_id = ?";
    const params = [userId];

    if (search.trim() !== "") {
      sql +=
        " AND (fokus_materi LIKE ? OR keterangan LIKE ? OR status_fokus LIKE ?)";
      const q = `%${search.trim()}%`;
      params.push(q, q, q);
    }

    sql += " ORDER BY id DESC";
    const [rows] = await dbPool.query(sql, params);

    res.json({
      status: "success",
      data: rows,
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// 5d. Tambah Catatan Fokus Belajar Baru (Create)
app.post("/api/profil/fokus", authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const { fokus_materi, keterangan, target_selesai, status_fokus } = req.body;

    if (!fokus_materi || fokus_materi.trim() === "") {
      return res.status(400).json({
        status: "error",
        message: "Fokus materi belajar tidak boleh kosong",
      });
    }

    const status = status_fokus || "Aktif";

    const [result] = await dbPool.query(
      "INSERT INTO catatan_fokus (user_id, fokus_materi, keterangan, target_selesai, status_fokus) VALUES (?, ?, ?, ?, ?)",
      [
        userId,
        fokus_materi.trim(),
        keterangan ? keterangan.trim() : null,
        target_selesai ? target_selesai.trim() : null,
        status,
      ],
    );

    res.status(201).json({
      status: "success",
      message: "Catatan fokus belajar berhasil ditambahkan",
      data: {
        id: result.insertId,
        user_id: userId,
        fokus_materi: fokus_materi.trim(),
        keterangan: keterangan ? keterangan.trim() : null,
        target_selesai: target_selesai ? target_selesai.trim() : null,
        status_fokus: status,
      },
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// 5e. Perbarui Catatan Fokus Belajar (Update)
app.put("/api/profil/fokus/:id", authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const fokusId = req.params.id;
    const { fokus_materi, keterangan, target_selesai, status_fokus } = req.body;

    if (!fokus_materi || fokus_materi.trim() === "") {
      return res.status(400).json({
        status: "error",
        message: "Fokus materi belajar tidak boleh kosong",
      });
    }

    const [result] = await dbPool.query(
      "UPDATE catatan_fokus SET fokus_materi = ?, keterangan = ?, target_selesai = ?, status_fokus = ? WHERE id = ? AND user_id = ?",
      [
        fokus_materi.trim(),
        keterangan ? keterangan.trim() : null,
        target_selesai ? target_selesai.trim() : null,
        status_fokus || "Aktif",
        fokusId,
        userId,
      ],
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({
        status: "error",
        message: "Catatan fokus tidak ditemukan atau bukan milik Anda",
      });
    }

    res.json({
      status: "success",
      message: "Catatan fokus belajar berhasil diperbarui",
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// 5f. Hapus Catatan Fokus Belajar (Delete)
app.delete("/api/profil/fokus/:id", authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const fokusId = req.params.id;

    const [result] = await dbPool.query(
      "DELETE FROM catatan_fokus WHERE id = ? AND user_id = ?",
      [fokusId, userId],
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({
        status: "error",
        message: "Catatan fokus tidak ditemukan atau bukan milik Anda",
      });
    }

    res.json({
      status: "success",
      message: "Catatan fokus belajar berhasil dihapus",
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// 6. Ringkasan Statistik Dashboard
app.get("/api/dashboard/statistik", authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const [bacaanRows] = await dbPool.query(
      'SELECT COUNT(*) AS total, SUM(CASE WHEN status_baca = "Sedang Dibaca" THEN 1 ELSE 0 END) AS sedang, SUM(CASE WHEN status_baca = "Selesai" THEN 1 ELSE 0 END) AS selesai FROM bahan_bacaan WHERE user_id = ?',
      [userId],
    );
    const [progresRows] = await dbPool.query(
      "SELECT COALESCE(SUM(halaman_dicapai), 0) AS total_halaman FROM catatan_progres WHERE user_id = ?",
      [userId],
    );

    res.json({
      status: "success",
      total_bacaan: bacaanRows[0].total || 0,
      sedang_dibaca: bacaanRows[0].sedang || 0,
      selesai_dibaca: bacaanRows[0].selesai || 0,
      total_halaman: progresRows[0].total_halaman || 0,
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// ==================== ENDPOINT CRUDS 1: KATEGORI TOPIK ====================

// 7. Ambil Daftar & Pencarian Kategori Topik (Read & Search)
app.get("/api/kategori", authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const search = req.query.search || "";

    let sql = `
            SELECT k.id, k.user_id, k.nama_topik, k.deskripsi, k.created_at,
                   COUNT(b.id) AS total_bacaan
            FROM kategori_topik k
            LEFT JOIN bahan_bacaan b ON b.kategori_id = k.id
            WHERE k.user_id = ?
        `;
    const params = [userId];

    if (search.trim() !== "") {
      sql += " AND k.nama_topik LIKE ?";
      params.push(`%${search.trim()}%`);
    }

    sql += " GROUP BY k.id ORDER BY k.id DESC";

    const [rows] = await dbPool.query(sql, params);
    res.json({
      status: "success",
      data: rows,
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// 8. Tambah Kategori Topik Baru (Create)
app.post("/api/kategori", authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const { nama_topik, deskripsi } = req.body;

    if (!nama_topik || nama_topik.trim() === "") {
      return res
        .status(400)
        .json({ status: "error", message: "Nama kategori topik wajib diisi" });
    }

    const [result] = await dbPool.query(
      "INSERT INTO kategori_topik (user_id, nama_topik, deskripsi) VALUES (?, ?, ?)",
      [userId, nama_topik.trim(), deskripsi ? deskripsi.trim() : null],
    );

    res.status(201).json({
      status: "success",
      message: "Kategori topik berhasil ditambahkan",
      id: result.insertId,
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// 9. Perbarui Kategori Topik (Update)
app.put("/api/kategori/:id", authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const kategoriId = req.params.id;
    const { nama_topik, deskripsi } = req.body;

    if (!nama_topik || nama_topik.trim() === "") {
      return res
        .status(400)
        .json({ status: "error", message: "Nama kategori topik wajib diisi" });
    }

    const [result] = await dbPool.query(
      "UPDATE kategori_topik SET nama_topik = ?, deskripsi = ? WHERE id = ? AND user_id = ?",
      [
        nama_topik.trim(),
        deskripsi ? deskripsi.trim() : null,
        kategoriId,
        userId,
      ],
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({
        status: "error",
        message: "Kategori tidak ditemukan atau bukan milik Anda",
      });
    }

    res.json({
      status: "success",
      message: "Kategori topik berhasil diperbarui",
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// 10. Hapus Kategori Topik (Delete)
app.delete("/api/kategori/:id", authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const kategoriId = req.params.id;

    const [result] = await dbPool.query(
      "DELETE FROM kategori_topik WHERE id = ? AND user_id = ?",
      [kategoriId, userId],
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({
        status: "error",
        message: "Kategori tidak ditemukan atau bukan milik Anda",
      });
    }

    res.json({
      status: "success",
      message: "Kategori topik berhasil dihapus",
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// ==================== ENDPOINT CRUDS 2: BAHAN BACAAN ====================

// 11. Ambil Daftar & Pencarian Bahan Bacaan (Read & Search)
app.get("/api/bacaan", authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const search = req.query.search || "";
    const kategoriId = req.query.kategori_id || "";
    const statusBaca = req.query.status_baca || "";

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

    if (search.trim() !== "") {
      sql += " AND (b.judul LIKE ? OR b.penulis_sumber LIKE ? OR k.nama_topik LIKE ?)";
      const term = `%${search.trim()}%`;
      params.push(term, term, term);
    }

    if (kategoriId.trim() !== "") {
      sql += " AND b.kategori_id = ?";
      params.push(kategoriId.trim());
    }

    if (statusBaca.trim() !== "" && statusBaca !== "Semua") {
      sql += " AND b.status_baca = ?";
      params.push(statusBaca.trim());
    }

    sql += " GROUP BY b.id ORDER BY b.id DESC";

    const [rows] = await dbPool.query(sql, params);
    res.json({
      status: "success",
      data: rows,
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// 12. Tambah Bahan Bacaan Baru (Create)
app.post("/api/bacaan", authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const { kategori_id, judul, penulis_sumber, total_halaman, status_baca } =
      req.body;

    if (!kategori_id || !judul || !penulis_sumber || !total_halaman) {
      return res.status(400).json({
        status: "error",
        message:
          "Kategori, judul, penulis/sumber, dan total halaman wajib diisi",
      });
    }

    const hal = parseInt(total_halaman, 10);
    if (isNaN(hal) || hal <= 0) {
      return res.status(400).json({
        status: "error",
        message: "Total halaman harus berupa angka positif",
      });
    }

    // Validasi kepemilikan kategori
    const [katCheck] = await dbPool.query(
      "SELECT id FROM kategori_topik WHERE id = ? AND user_id = ?",
      [kategori_id, userId],
    );
    if (katCheck.length === 0) {
      return res.status(400).json({
        status: "error",
        message: "Kategori topik yang dipilih tidak valid",
      });
    }

    const statusValid = ["Belum Dibaca", "Sedang Dibaca", "Selesai"];
    const statusFinal = statusValid.includes(status_baca)
      ? status_baca
      : "Belum Dibaca";

    const [result] = await dbPool.query(
      "INSERT INTO bahan_bacaan (user_id, kategori_id, judul, penulis_sumber, total_halaman, status_baca) VALUES (?, ?, ?, ?, ?, ?)",
      [
        userId,
        kategori_id,
        judul.trim(),
        penulis_sumber.trim(),
        hal,
        statusFinal,
      ],
    );

    res.status(201).json({
      status: "success",
      message: "Bahan bacaan berhasil ditambahkan",
      id: result.insertId,
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// 13. Perbarui Bahan Bacaan (Update)
app.put("/api/bacaan/:id", authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const bacaanId = req.params.id;
    const { kategori_id, judul, penulis_sumber, total_halaman, status_baca } =
      req.body;

    if (!kategori_id || !judul || !penulis_sumber || !total_halaman) {
      return res.status(400).json({
        status: "error",
        message:
          "Kategori, judul, penulis/sumber, dan total halaman wajib diisi",
      });
    }

    const hal = parseInt(total_halaman, 10);
    if (isNaN(hal) || hal <= 0) {
      return res.status(400).json({
        status: "error",
        message: "Total halaman harus berupa angka positif",
      });
    }

    const statusValid = ["Belum Dibaca", "Sedang Dibaca", "Selesai"];
    const statusFinal = statusValid.includes(status_baca)
      ? status_baca
      : "Belum Dibaca";

    const [result] = await dbPool.query(
      "UPDATE bahan_bacaan SET kategori_id = ?, judul = ?, penulis_sumber = ?, total_halaman = ?, status_baca = ? WHERE id = ? AND user_id = ?",
      [
        kategori_id,
        judul.trim(),
        penulis_sumber.trim(),
        hal,
        statusFinal,
        bacaanId,
        userId,
      ],
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({
        status: "error",
        message: "Bahan bacaan tidak ditemukan atau bukan milik Anda",
      });
    }

    res.json({
      status: "success",
      message: "Bahan bacaan berhasil diperbarui",
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// 14. Hapus Bahan Bacaan (Delete)
app.delete("/api/bacaan/:id", authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const bacaanId = req.params.id;

    const [result] = await dbPool.query(
      "DELETE FROM bahan_bacaan WHERE id = ? AND user_id = ?",
      [bacaanId, userId],
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({
        status: "error",
        message: "Bahan bacaan tidak ditemukan atau bukan milik Anda",
      });
    }

    res.json({
      status: "success",
      message: "Bahan bacaan berhasil dihapus",
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// ==================== ENDPOINT CRUDS 3: CATATAN PROGRES ====================

// 15. Ambil Riwayat & Pencarian Catatan Progres (Read & Search)
app.get("/api/progres", authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const search = req.query.search || "";
    const bacaanId = req.query.bacaan_id || "";

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

    if (search.trim() !== "") {
      sql +=
        " AND (cp.ringkasan_materi LIKE ? OR cp.tanggal_baca LIKE ? OR b.judul LIKE ?)";
      const term = `%${search.trim()}%`;
      params.push(term, term, term);
    }

    if (bacaanId.trim() !== "") {
      sql += " AND cp.bacaan_id = ?";
      params.push(bacaanId.trim());
    }

    sql += " ORDER BY cp.id DESC";

    const [rows] = await dbPool.query(sql, params);
    res.json({
      status: "success",
      data: rows,
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// 16. Tambah Catatan Progres Baru (Create)
app.post("/api/progres", authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const {
      bacaan_id,
      tanggal_baca,
      halaman_dicapai,
      ringkasan_materi,
      target_harian,
    } = req.body;

    if (
      !bacaan_id ||
      !tanggal_baca ||
      halaman_dicapai === undefined ||
      !ringkasan_materi
    ) {
      return res.status(400).json({
        status: "error",
        message:
          "Bahan bacaan, tanggal baca, halaman dicapai, dan ringkasan materi wajib diisi",
      });
    }

    const hal = parseInt(halaman_dicapai, 10);
    if (isNaN(hal) || hal < 0) {
      return res.status(400).json({
        status: "error",
        message: "Halaman dicapai harus berupa angka positif atau nol",
      });
    }

    const target = target_harian ? parseInt(target_harian, 10) : hal;

    // Validasi kepemilikan bahan bacaan
    const [bacaanCheck] = await dbPool.query(
      "SELECT id, total_halaman, status_baca FROM bahan_bacaan WHERE id = ? AND user_id = ?",
      [bacaan_id, userId],
    );
    if (bacaanCheck.length === 0) {
      return res.status(400).json({
        status: "error",
        message: "Bahan bacaan yang dipilih tidak valid",
      });
    }

    const [result] = await dbPool.query(
      "INSERT INTO catatan_progres (user_id, bacaan_id, tanggal_baca, halaman_dicapai, ringkasan_materi, target_harian) VALUES (?, ?, ?, ?, ?, ?)",
      [
        userId,
        bacaan_id,
        tanggal_baca.trim(),
        hal,
        ringkasan_materi.trim(),
        isNaN(target) ? 0 : target,
      ],
    );

    // Otomatis update status bahan bacaan jika halaman dicapai >= total halaman
    const bacaanItem = bacaanCheck[0];
    if (
      hal >= bacaanItem.total_halaman &&
      bacaanItem.status_baca !== "Selesai"
    ) {
      await dbPool.query(
        'UPDATE bahan_bacaan SET status_baca = "Selesai" WHERE id = ?',
        [bacaan_id],
      );
    } else if (hal > 0 && bacaanItem.status_baca === "Belum Dibaca") {
      await dbPool.query(
        'UPDATE bahan_bacaan SET status_baca = "Sedang Dibaca" WHERE id = ?',
        [bacaan_id],
      );
    }

    res.status(201).json({
      status: "success",
      message: "Catatan progres berhasil ditambahkan",
      id: result.insertId,
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// 17. Perbarui Catatan Progres (Update)
app.put("/api/progres/:id", authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const progresId = req.params.id;
    const {
      bacaan_id,
      tanggal_baca,
      halaman_dicapai,
      ringkasan_materi,
      target_harian,
    } = req.body;

    if (
      !bacaan_id ||
      !tanggal_baca ||
      halaman_dicapai === undefined ||
      !ringkasan_materi
    ) {
      return res.status(400).json({
        status: "error",
        message:
          "Bahan bacaan, tanggal baca, halaman dicapai, dan ringkasan materi wajib diisi",
      });
    }

    const hal = parseInt(halaman_dicapai, 10);
    if (isNaN(hal) || hal < 0) {
      return res.status(400).json({
        status: "error",
        message: "Halaman dicapai harus berupa angka positif atau nol",
      });
    }

    const target = target_harian ? parseInt(target_harian, 10) : hal;

    const [result] = await dbPool.query(
      "UPDATE catatan_progres SET bacaan_id = ?, tanggal_baca = ?, halaman_dicapai = ?, ringkasan_materi = ?, target_harian = ? WHERE id = ? AND user_id = ?",
      [
        bacaan_id,
        tanggal_baca.trim(),
        hal,
        ringkasan_materi.trim(),
        isNaN(target) ? 0 : target,
        progresId,
        userId,
      ],
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({
        status: "error",
        message: "Catatan progres tidak ditemukan atau bukan milik Anda",
      });
    }

    res.json({
      status: "success",
      message: "Catatan progres berhasil diperbarui",
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// 18. Hapus Catatan Progres (Delete)
app.delete("/api/progres/:id", authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const progresId = req.params.id;

    const [result] = await dbPool.query(
      "DELETE FROM catatan_progres WHERE id = ? AND user_id = ?",
      [progresId, userId],
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({
        status: "error",
        message: "Catatan progres tidak ditemukan atau bukan milik Anda",
      });
    }

    res.json({
      status: "success",
      message: "Catatan progres berhasil dihapus",
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// ==================== ENDPOINT CRUDS 4: TARGET BACA PERIODIK ====================

// 19. Ambil Daftar & Pencarian Target Baca (Read & Search)
app.get("/api/target", authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const search = req.query.search || "";

    let sql = `
            SELECT t.id, t.user_id, t.bacaan_id, t.target_halaman, t.deadline, t.status_target, t.created_at,
                   bb.judul AS judul_bacaan, bb.total_halaman, bb.status_baca,
                   kt.nama_topik AS nama_kategori,
                   COALESCE(MAX(cp.halaman_dicapai), 0) AS capaian_tertinggi
            FROM target_baca t
            JOIN bahan_bacaan bb ON t.bacaan_id = bb.id
            LEFT JOIN kategori_topik kt ON bb.kategori_id = kt.id
            LEFT JOIN catatan_progres cp ON cp.bacaan_id = bb.id AND cp.user_id = t.user_id
            WHERE t.user_id = ?
        `;
    const params = [userId];

    if (search.trim() !== "") {
      sql +=
        " AND (bb.judul LIKE ? OR t.deadline LIKE ? OR t.status_target LIKE ? OR kt.nama_topik LIKE ?)";
      const q = `%${search.trim()}%`;
      params.push(q, q, q, q);
    }

    sql += " GROUP BY t.id ORDER BY t.id DESC";

    const [rows] = await dbPool.query(sql, params);

    res.json({
      status: "success",
      data: rows,
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// 20. Tambah Target Baca Baru (Create)
app.post("/api/target", authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const { bacaan_id, target_halaman, deadline, status_target } = req.body;

    if (!bacaan_id || !target_halaman || !deadline) {
      return res.status(400).json({
        status: "error",
        message: "Bahan bacaan, target halaman, dan tenggat waktu wajib diisi",
      });
    }

    const hal = parseInt(target_halaman, 10);
    if (isNaN(hal) || hal <= 0) {
      return res.status(400).json({
        status: "error",
        message: "Target halaman harus berupa angka lebih dari 0",
      });
    }

    // Verifikasi kepemilikan bacaan (IDOR Protection)
    const [bacaanRows] = await dbPool.query(
      "SELECT id FROM bahan_bacaan WHERE id = ? AND user_id = ?",
      [bacaan_id, userId],
    );
    if (bacaanRows.length === 0) {
      return res.status(403).json({
        status: "error",
        message: "Bahan bacaan tidak valid atau Anda tidak memiliki hak akses",
      });
    }

    const status = status_target || "Sedang Berjalan";

    const [result] = await dbPool.query(
      "INSERT INTO target_baca (user_id, bacaan_id, target_halaman, deadline, status_target) VALUES (?, ?, ?, ?, ?)",
      [userId, bacaan_id, hal, deadline.trim(), status],
    );

    res.status(201).json({
      status: "success",
      message: "Target baca berhasil ditetapkan",
      data: {
        id: result.insertId,
        user_id: userId,
        bacaan_id,
        target_halaman: hal,
        deadline: deadline.trim(),
        status_target: status,
      },
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// 21. Perbarui Target Baca (Update)
app.put("/api/target/:id", authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const targetId = req.params.id;
    const { bacaan_id, target_halaman, deadline, status_target } = req.body;

    if (!target_halaman || !deadline) {
      return res.status(400).json({
        status: "error",
        message: "Target halaman dan tenggat waktu wajib diisi",
      });
    }

    const hal = parseInt(target_halaman, 10);
    if (isNaN(hal) || hal <= 0) {
      return res.status(400).json({
        status: "error",
        message: "Target halaman harus berupa angka lebih dari 0",
      });
    }

    let sql =
      "UPDATE target_baca SET target_halaman = ?, deadline = ?, status_target = ?";
    const params = [hal, deadline.trim(), status_target || "Sedang Berjalan"];

    if (bacaan_id) {
      // Verifikasi kepemilikan bacaan jika bacaan_id diubah
      const [bacaanRows] = await dbPool.query(
        "SELECT id FROM bahan_bacaan WHERE id = ? AND user_id = ?",
        [bacaan_id, userId],
      );
      if (bacaanRows.length === 0) {
        return res.status(403).json({
          status: "error",
          message: "Bahan bacaan tidak valid atau bukan milik Anda",
        });
      }
      sql += ", bacaan_id = ?";
      params.push(bacaan_id);
    }

    sql += " WHERE id = ? AND user_id = ?";
    params.push(targetId, userId);

    const [result] = await dbPool.query(sql, params);

    if (result.affectedRows === 0) {
      return res.status(404).json({
        status: "error",
        message: "Target baca tidak ditemukan atau bukan milik Anda",
      });
    }

    res.json({
      status: "success",
      message: "Target baca berhasil diperbarui",
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// 22. Hapus Target Baca (Delete)
app.delete("/api/target/:id", authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const targetId = req.params.id;

    const [result] = await dbPool.query(
      "DELETE FROM target_baca WHERE id = ? AND user_id = ?",
      [targetId, userId],
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({
        status: "error",
        message: "Target baca tidak ditemukan atau bukan milik Anda",
      });
    }

    res.json({
      status: "success",
      message: "Target baca berhasil dihapus",
    });
  } catch (error) {
    res.status(500).json({ status: "error", message: error.message });
  }
});

// Inisialisasi Skema Tabel Otomatis saat Startup (6 Entitas Relasional)
async function initDatabaseSchema() {
  try {
    await dbPool.query(
      "ALTER TABLE users ADD COLUMN IF NOT EXISTS catatan_fokus TEXT NULL AFTER bio_belajar",
    );
    await dbPool.query(`
            CREATE TABLE IF NOT EXISTS target_baca (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                bacaan_id INT NOT NULL,
                target_halaman INT NOT NULL,
                deadline VARCHAR(20) NOT NULL,
                status_target ENUM('Sedang Berjalan', 'Tercapai', 'Tertunda') DEFAULT 'Sedang Berjalan',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (bacaan_id) REFERENCES bahan_bacaan(id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        `);
    await dbPool.query(`
            CREATE TABLE IF NOT EXISTS catatan_fokus (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                fokus_materi VARCHAR(150) NOT NULL,
                keterangan TEXT NULL,
                target_selesai VARCHAR(20) NULL,
                status_fokus ENUM('Aktif', 'Selesai') DEFAULT 'Aktif',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        `);
    console.log(
      "[BacaLog API] Skema 6 tabel database relasional terverifikasi aktif.",
    );
  } catch (err) {
    console.error(
      "[BacaLog API] Gagal memverifikasi skema database:",
      err.message,
    );
  }
}

// Jalankan Server
initDatabaseSchema().then(() => {
  app.listen(PORT, "0.0.0.0", () => {
    console.log(`[BacaLog API] Server aktif di port ${PORT}`);
    console.log(
      `[BacaLog API] Android Emulator Base URL: http://10.0.2.2:${PORT}/api/`,
    );
  });
});

-- Skema Database MySQL: BacaLog (Pencatat Progres Bacaan dan Belajar Mandiri)
-- Mahasiswa: Ilham Saputra (NIM: 20240140118) - Kelas D

CREATE DATABASE IF NOT EXISTS `bacalog_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `bacalog_db`;

-- 1. Tabel Users (Autentikasi & Profil Pengguna)
CREATE TABLE IF NOT EXISTS `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `nama` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `bio_belajar` VARCHAR(255) DEFAULT 'Semangat belajar dan membaca setiap hari!',
    `refresh_token` TEXT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Tabel Kategori Topik (CRUDS 1)
CREATE TABLE IF NOT EXISTS `kategori_topik` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `nama_topik` VARCHAR(100) NOT NULL,
    `deskripsi` TEXT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_kategori_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 3. Tabel Bahan Bacaan (CRUDS 2)
CREATE TABLE IF NOT EXISTS `bahan_bacaan` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `kategori_id` INT NOT NULL,
    `judul` VARCHAR(150) NOT NULL,
    `penulis_sumber` VARCHAR(100) NOT NULL,
    `total_halaman` INT NOT NULL,
    `status_baca` ENUM('Belum Dibaca', 'Sedang Dibaca', 'Selesai') DEFAULT 'Belum Dibaca',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_bacaan_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_bacaan_kategori` FOREIGN KEY (`kategori_id`) REFERENCES `kategori_topik` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. Tabel Catatan Progres (CRUDS 3)
CREATE TABLE IF NOT EXISTS `catatan_progres` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `bacaan_id` INT NOT NULL,
    `tanggal_baca` VARCHAR(20) NOT NULL,
    `halaman_dicapai` INT NOT NULL,
    `ringkasan_materi` TEXT NOT NULL,
    `target_harian` INT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_progres_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_progres_bacaan` FOREIGN KEY (`bacaan_id`) REFERENCES `bahan_bacaan` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. Tabel Target Baca Periodik (CRUDS 4)
CREATE TABLE IF NOT EXISTS `target_baca` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `bacaan_id` INT NOT NULL,
    `target_halaman` INT NOT NULL,
    `deadline` VARCHAR(20) NOT NULL,
    `status_target` ENUM('Sedang Berjalan', 'Tercapai', 'Tertunda') DEFAULT 'Sedang Berjalan',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_target_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_target_bacaan` FOREIGN KEY (`bacaan_id`) REFERENCES `bahan_bacaan` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. Tabel Catatan Fokus Belajar (CRUDS 5)
CREATE TABLE IF NOT EXISTS `catatan_fokus` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `fokus_materi` VARCHAR(150) NOT NULL,
    `keterangan` TEXT NULL,
    `target_selesai` VARCHAR(20) NULL,
    `status_fokus` ENUM('Aktif', 'Selesai') DEFAULT 'Aktif',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_fokus_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Data Awal Pengujian (Seeder Demo)
INSERT INTO `users` (`id`, `nama`, `email`, `password`, `bio_belajar`) 
VALUES (1, 'Ilham Saputra', 'ilham@bacalog.com', '$2b$10$wE0p7Q43Q.mX4c9U7V.Jv.3JbU5E0/8U1u8g6y4Q0Y/m0vQnB8bKe', 'Fokus belajar Jetpack Compose & Arsitektur MVVM')
ON DUPLICATE KEY UPDATE `nama` = VALUES(`nama`);

INSERT INTO `kategori_topik` (`id`, `user_id`, `nama_topik`, `deskripsi`) VALUES
(1, 1, 'Pengembangan Android', 'Topik seputar Kotlin, Jetpack Compose, dan arsitektur aplikasi mobile.'),
(2, 1, 'Basis Data & Backend', 'Materi perancangan database relasional MySQL dan RESTful API.')
ON DUPLICATE KEY UPDATE `nama_topik` = VALUES(`nama_topik`);

INSERT INTO `bahan_bacaan` (`id`, `user_id`, `kategori_id`, `judul`, `penulis_sumber`, `total_halaman`, `status_baca`) VALUES
(1, 1, 1, 'Panduan Praktis Jetpack Compose', 'Google Developers', 250, 'Sedang Dibaca'),
(2, 1, 2, 'Fundamental MySQL & REST API', 'Oracle Press', 180, 'Belum Dibaca')
ON DUPLICATE KEY UPDATE `judul` = VALUES(`judul`);

INSERT INTO `catatan_progres` (`id`, `user_id`, `bacaan_id`, `tanggal_baca`, `halaman_dicapai`, `ringkasan_materi`, `target_harian`) VALUES
(1, 1, 1, '2026-11-22', 45, 'Mempelajari dasar Navigation Compose dan state hoisting pada TextField.', 20)
ON DUPLICATE KEY UPDATE `halaman_dicapai` = VALUES(`halaman_dicapai`);

INSERT INTO `target_baca` (`id`, `user_id`, `bacaan_id`, `target_halaman`, `deadline`, `status_target`) VALUES
(1, 1, 1, 150, '2026-11-30', 'Sedang Berjalan')
ON DUPLICATE KEY UPDATE `target_halaman` = VALUES(`target_halaman`);

INSERT INTO `catatan_fokus` (`id`, `user_id`, `fokus_materi`, `keterangan`, `target_selesai`, `status_fokus`) VALUES
(1, 1, 'Arsitektur MVVM & StateFlow', 'Pendalaman pemisahan state UI pada Compose dan Repository Pattern.', '2026-11-29', 'Aktif')
ON DUPLICATE KEY UPDATE `fokus_materi` = VALUES(`fokus_materi`);

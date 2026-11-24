const mysql = require('mysql2/promise');

async function migrate() {
    const pool = mysql.createPool({
        host: 'localhost',
        user: 'root',
        password: '',
        database: 'bacalog_db'
    });

    try {
        console.log('1. Checking & adding catatan_fokus to users...');
        await pool.query('ALTER TABLE users ADD COLUMN IF NOT EXISTS catatan_fokus TEXT NULL AFTER bio_belajar');
        console.log('   users.catatan_fokus OK');

        console.log('2. Creating target_baca table if not exists...');
        await pool.query(`
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
        console.log('   target_baca table OK');

        const [tables] = await pool.query('SHOW TABLES');
        console.log('3. All tables in bacalog_db:');
        tables.forEach(t => console.log('   -', Object.values(t)[0]));

        const [userCols] = await pool.query('DESCRIBE users');
        console.log('4. Users columns:', userCols.map(c => c.Field).join(', '));

        const [targetCols] = await pool.query('DESCRIBE target_baca');
        console.log('5. Target_baca columns:', targetCols.map(c => c.Field).join(', '));
    } catch (err) {
        console.error('Migration failed:', err);
    } finally {
        await pool.end();
    }
}

migrate();

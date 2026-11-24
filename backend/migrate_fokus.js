const mysql = require('mysql2/promise');

async function migrateFocusTable() {
    const pool = mysql.createPool({
        host: 'localhost',
        user: 'root',
        password: '',
        database: 'bacalog_db'
    });

    try {
        console.log('1. Creating catatan_fokus table...');
        await pool.query(`
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
        console.log('   catatan_fokus table created successfully!');

        const [tables] = await pool.query('SHOW TABLES');
        console.log('2. All 6 tables in bacalog_db:');
        tables.forEach(t => console.log('   -', Object.values(t)[0]));

        const [cols] = await pool.query('DESCRIBE catatan_fokus');
        console.log('3. catatan_fokus columns:', cols.map(c => c.Field).join(', '));
    } catch (err) {
        console.error('Migration failed:', err.message);
    } finally {
        await pool.end();
    }
}

migrateFocusTable();

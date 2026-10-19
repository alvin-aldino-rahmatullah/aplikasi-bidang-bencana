-- Schema modul petugas. Jalankan di MySQL database bantuan_bencana.
-- Asumsi tabel users sudah ada (modul login).
-- ddl-auto=validate: Hibernate tidak membuat tabel ini.

USE bantuan_bencana;

CREATE TABLE IF NOT EXISTS bencana (
    id INT NOT NULL AUTO_INCREMENT,
    nama VARCHAR(100) NOT NULL,
    keterangan TEXT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_bencana_nama (nama)
);

CREATE TABLE IF NOT EXISTS laporan (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    bencana_id INT NOT NULL,
    judul VARCHAR(200) NOT NULL,
    lokasi VARCHAR(255) NOT NULL,
    deskripsi TEXT NULL,
    waktu_kejadian DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_laporan_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_laporan_bencana FOREIGN KEY (bencana_id) REFERENCES bencana (id)
);

CREATE TABLE IF NOT EXISTS dokumentasi (
    id INT NOT NULL AUTO_INCREMENT,
    laporan_id INT NOT NULL,
    nama_file VARCHAR(255) NOT NULL,
    path_file VARCHAR(500) NOT NULL,
    uploaded_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_dokumentasi_laporan FOREIGN KEY (laporan_id) REFERENCES laporan (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS kebutuhan_bantuan (
    id INT NOT NULL AUTO_INCREMENT,
    laporan_id INT NOT NULL,
    nama_item VARCHAR(150) NOT NULL,
    jumlah INT NOT NULL,
    satuan VARCHAR(50) NOT NULL,
    catatan VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_kebutuhan_laporan FOREIGN KEY (laporan_id) REFERENCES laporan (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS feedback (
    id INT NOT NULL AUTO_INCREMENT,
    laporan_id INT NOT NULL,
    admin_id INT NULL,
    isi TEXT NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_feedback_laporan FOREIGN KEY (laporan_id) REFERENCES laporan (id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_admin FOREIGN KEY (admin_id) REFERENCES users (id)
);

INSERT IGNORE INTO bencana (nama, keterangan) VALUES
    ('Banjir', 'Banjir / genangan'),
    ('Gempa', 'Gempa bumi'),
    ('Tanah longsor', 'Longsor / pergerakan tanah'),
    ('Kebakaran', 'Kebakaran lahan atau permukiman');

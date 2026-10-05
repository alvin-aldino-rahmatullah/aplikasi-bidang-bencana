# REWANG - Aplikasi Bantuan Bencana Alam

**REWANG (Relawan Evakuasi Wilayah Aman Nusantara Gotong-royong)** adalah aplikasi berbasis web yang dibuat untuk membantu proses pengelolaan dan penyaluran bantuan pada kondisi bencana alam.

Aplikasi ini membantu mengelola laporan bencana, kebutuhan bantuan, stok barang/logistik, dokumentasi, serta proses verifikasi bantuan. Selain itu, terdapat fitur Chat AI yang dapat digunakan sebagai pendukung untuk memperoleh informasi terkait kondisi bencana.

## 🎯 Tujuan

REWANG dibuat untuk membantu proses pengelolaan bantuan bencana agar lebih terstruktur, terutama dalam:

- Mengelola laporan bencana.
- Mendata kebutuhan bantuan berdasarkan laporan.
- Mengelola stok barang atau logistik.
- Melakukan verifikasi laporan bencana.
- Mendokumentasikan kondisi bencana.
- Memberikan feedback terhadap laporan.
- Membantu pengguna memperoleh informasi melalui Chat AI.

## 👥 Role Pengguna

Aplikasi memiliki dua role utama:

### Admin

Admin memiliki akses untuk:

- Mengelola user.
- Mengakses dashboard.
- Mengelola stok barang.
- Melihat statistik.
- Memverifikasi laporan bencana.
- Memberikan feedback terhadap laporan.
- Login dan logout.

### Petugas

Petugas memiliki akses untuk:

- Mengakses dashboard.
- Membuat dan mengelola laporan bencana.
- Membuat dan mengirim laporan.
- Mendata kebutuhan bantuan.
- Mengunggah dokumentasi bencana.
- Melihat feedback dari Admin.
- Menggunakan Chat AI.
- Login dan logout.

## 🚀 Fitur Utama

### 1. Laporan Bencana
Petugas dapat membuat dan mengelola laporan terkait kejadian bencana.

### 2. Kebutuhan Bantuan
Petugas dapat mencatat kebutuhan bantuan berdasarkan laporan bencana, seperti barang atau logistik yang diperlukan.

### 3. Manajemen Barang
Admin dapat mengelola data stok barang yang tersedia untuk kebutuhan bantuan.

### 4. Verifikasi Laporan
Admin dapat melakukan verifikasi terhadap laporan yang dikirim oleh petugas.

### 5. Dokumentasi Bencana
Laporan dapat dilengkapi dengan dokumentasi atau foto kondisi bencana.

### 6. Feedback
Admin dapat memberikan feedback terhadap laporan yang telah dibuat oleh petugas.

### 7. Chat AI
Tersedia fitur Chat AI sebagai pendukung untuk memperoleh informasi atau bantuan terkait kondisi bencana.

### 8. Dashboard & Statistik
Admin dan Petugas memiliki akses dashboard sesuai dengan role masing-masing.

## 🗂️ Struktur Data

Beberapa entity utama yang digunakan dalam aplikasi:

- Users
- Roles
- Permissions
- Role_Permissions
- Bencana
- Laporan
- Dokumentasi
- Feedback
- Kebutuhan Bantuan
- Barang
- Chat AI

Relasi antar entity digunakan untuk menghubungkan pengguna, laporan bencana, dokumentasi, kebutuhan bantuan, barang, dan feedback.

## 🛠️ Teknologi yang Digunakan

### Backend
- Java
- Spring Boot

### Database
- MySQL

### Development
- Visual Studio Code
- Git
- GitHub

## 🏗️ Metodologi Pengembangan

Pengembangan aplikasi menggunakan metode **Agile** agar proses pengembangan dapat dilakukan secara bertahap dan fleksibel.

Tahapan yang digunakan:

1. Analisis Kebutuhan
2. Planning
3. Design
4. Development
5. Testing
6. Evaluation
7. Iteration
8. Release

## 📊 Database

Aplikasi menggunakan **MySQL** sebagai DBMS karena data yang digunakan memiliki struktur dan hubungan antar tabel.

Database mencakup data seperti:

- User dan role
- Laporan bencana
- Dokumentasi
- Kebutuhan bantuan
- Barang/logistik
- Feedback
- Chat AI

## 👨‍💻 Tim Pengembang

**Kelompok Pecinta Mie Ayam**

Project ini dibuat sebagai bagian dari pengembangan aplikasi berbasis web untuk bidang kebencanaan.

## 📌 Status Project

> 🚧 **Dalam Pengembangan**

Fitur dan sistem masih dalam tahap pengembangan dan pengujian.

## 📜 License

Project ini dibuat untuk keperluan pembelajaran dan pengembangan aplikasi.

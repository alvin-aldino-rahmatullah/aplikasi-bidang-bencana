# PRD Modul Petugas — REWANG

Dokumen ini adalah **kontrak kerja modul Petugas** untuk manusia (pemilik tugas) dan untuk AI. Bukan kode aplikasi. Jangan implementasi fitur di luar checklist yang sedang dikerjakan.

**Aplikasi:** REWANG (Relawan Evakuasi Wilayah Aman Nusantara Gotong-royong)  
**Repo:** `aplikasi-bidang-bencana`  
**Modul:** seluruh fitur role **PETUGAS** (kecuali login yang sudah dikerjakan Alvin)  
**Pemilik modul:** Rafi + 1 teman petugas  
**Chat AI:** ditunda (lihat P2-02, jangan dikerjakan sekarang)

---

## 0. Cara pakai (wajib dibaca AI pengganti)

Jika sesi AI sebelumnya limit, pindah ke AI lain, atau memulai chat baru:

1. Baca dokumen ini dari atas sampai checklist.
2. Cari item checklist **pertama** yang masih `[ ]` (status `TODO`), urutan **P0 → P1 → P2**.
3. Kerjakan **satu tugas** itu sampai kriteria selesai terpenuhi.
4. Setelah selesai, update item tersebut:
   - Ubah `[ ]` menjadi `[x]`
   - Ubah `Status: TODO` menjadi `Status: DONE`
   - Isi **Selesai:** tanggal, **File:** yang berubah, **Catatan AI:** ringkas apa yang dikerjakan dan apa yang AI berikutnya harus tahu
5. Jangan kerjakan bagian **Out of scope**.
6. Jangan `git commit` / `git push` kecuali pemilik modul memintanya secara eksplisit.
7. Jangan mengubah file plan Cursor. Update **hanya** checklist di dokumen ini.

Prompt siap tempel untuk AI pengganti:

```text
Baca docs/PRD-PETUGAS.md. Kerjakan item TODO pertama yang belum dicentang (urutan P0 → P1 → P2).
Ikuti batas AI vs manusia vs out of scope. Setelah selesai, update checklist di PRD itu.
Jangan commit kecuali saya minta. Jangan kerjakan Chat AI (P2-02) atau fitur Admin.
```

---

## 1. Konteks produk

REWANG adalah aplikasi web untuk pengelolaan dan penyaluran bantuan bencana alam. Tech stack yang sudah dipakai di repo:

| Layer | Teknologi |
|--------|-----------|
| Backend | Java 17, Spring Boot 4.1.1, Spring Security, Spring Data JPA, Validation |
| Frontend | Thymeleaf + Bootstrap 5 (CDN) |
| Database | MySQL, schema `bantuan_bencana`, `spring.jpa.hibernate.ddl-auto=validate` |
| Auth | Form login email + password hash, redirect per role |

Dua role utama (dari README):

- **Admin** — kelola user, stok barang, statistik, verifikasi laporan, beri feedback. **Bukan bagian modul ini.**
- **Petugas** — dashboard, CRUD laporan bencana, kirim laporan, data kebutuhan bantuan, unggah dokumentasi, lihat feedback admin, Chat AI (ditunda), login/logout (login sudah ada).

Login, entity `User` / `Role`, dan Spring Security sudah ada di pekerjaan Alvin (`feature/login`). Dashboard petugas masih template kosong.

---

## 2. Pembagian tanggung jawab

### 2.1 Yang kamu emban (pemilik modul Petugas)

| ID | Tugas manusia | AI tidak menggantikan |
|----|----------------|------------------------|
| H-01 | Sepakati **nama branch** dengan Alvin (contoh yang sudah ada: `feature/login`). Jangan buat branch sebelum nama disepakati. | AI hanya siapkan perintah setelah nama diputuskan. |
| H-02 | Pastikan MySQL database `bantuan_bencana` berjalan di mesin lokal. | AI tidak menginstal MySQL di komputer kamu. |
| H-03 | Jangan commit password DB, API key, atau secret. `application.properties` saat ini password kosong — jangan isi password nyata lalu di-push. | AI tidak menulis secret ke Git. |
| H-04 | Review hasil AI: baca diff, tes di browser sebagai user **PETUGAS**. | AI boleh menjalankan tes otomatis; keputusan “lolos tugas kelompok” ada di kamu. |
| H-05 | Push / buka PR ke GitHub setelah kamu setuju. | AI tidak push kecuali kamu minta. |
| H-06 | Koordinasi dengan teman petugas: siapa mengerjakan sub-fitur, kapan merge. | AI tidak memutuskan pembagian orang. |
| H-07 | Laporkan progres ke kelompok. | — |

### 2.2 Yang AI boleh kerjakan

- Entity, repository, service, controller, form DTO, validasi, template Thymeleaf, upload file, tes untuk **fitur petugas** di checklist.
- Navigasi dan isi dashboard petugas (ringkasan + tautan ke fitur petugas).
- Script SQL schema tabel petugas (karena `ddl-auto=validate`, tabel harus ada di MySQL).
- Update checklist PRD ini setiap tugas selesai.
- Perintah git **lokal** (checkout/branch) hanya setelah kamu setujui nama branch (P0-01).

### 2.3 Out of scope — jangan dikerjakan

| Area | Alasan |
|------|--------|
| Login/logout, halaman `login.html`, `AuthController` | Milik Alvin |
| Entity `User`, `Role`, `UserRepository`, `CustomUserDetailsService` | Sudah ada; jangan diubah kecuali blocker yang kamu setujui |
| `SecurityConfig` | Jangan rewrite. Path `/petugas/**` sudah `hasRole("PETUGAS")`. Perubahan hanya jika ada path petugas baru di luar `/petugas/**` dan kamu minta |
| Admin: kelola user, stok barang, statistik admin, **aksi** verifikasi laporan, **aksi** beri feedback | Modul Admin |
| Tabel `Barang`, `Permissions`, `Role_Permissions` | Bukan modul petugas |
| Chat AI (P2-02) | Ditunda |
| Commit/push tanpa permintaan pemilik | Aturan kerja |
| Mengubah password/secret di file yang ter-track Git | Keamanan |

Petugas **boleh membaca** status verifikasi dan feedback yang ditulis admin; petugas **tidak** mengubah status menjadi terverifikasi dan **tidak** menulis feedback admin.

---

## 3. Status kode sekarang (titik mulai)

Asumsi workspace: folder `rewang/` (Spring Boot Maven).

### Sudah ada

| File | Fungsi |
|------|--------|
| `rewang/src/main/java/com/example/rewang/config/SecurityConfig.java` | `/login` public; `/admin/**` ADMIN; `/petugas/**` PETUGAS; form login email; redirect dashboard |
| `rewang/src/main/java/com/example/rewang/controller/AuthController.java` | GET `/login` |
| `rewang/src/main/java/com/example/rewang/controller/DashboardController.java` | GET `/admin/dashboard`, GET `/petugas/dashboard` |
| `rewang/src/main/java/com/example/rewang/entity/User.java` | Tabel `users` |
| `rewang/src/main/java/com/example/rewang/entity/Role.java` | Tabel `roles`, field `namaRole` |
| `rewang/src/main/java/com/example/rewang/repository/UserRepository.java` | `findByEmail` |
| `rewang/src/main/java/com/example/rewang/service/CustomUserDetailsService.java` | Role dari `namaRole.toUpperCase()` → `ROLE_ADMIN` / `ROLE_PETUGAS` |
| `rewang/src/main/resources/templates/login.html` | Form login |
| `rewang/src/main/resources/templates/admin/dashboard.html` | Dashboard admin (template) |
| `rewang/src/main/resources/templates/petugas/dashboard.html` | Judul + logout saja |
| `rewang/src/main/resources/application.properties` | JDBC MySQL `bantuan_bencana`, `ddl-auto=validate` |

### Belum ada (harus dibuat lewat checklist)

- Entity: `Bencana`, `Laporan`, `Dokumentasi`, `KebutuhanBantuan`, `Feedback`
- Repository / service / controller petugas selain dashboard kosong
- Template CRUD petugas
- SQL DDL tabel petugas
- Branch kerja: `feature/dokumentasi` (lokal, dari `main` setelah pull sidebar; belum di-push)

---

## 4. Model data modul petugas

Selaras README, tanpa tabel Admin. Semua relasi ke pelapor memakai `users.id` yang sudah ada.

### 4.1 `bencana`

Master jenis/kejadian (misalnya banjir, gempa). Dipakai dropdown saat buat laporan.

| Kolom | Tipe (usulan) | Keterangan |
|-------|----------------|------------|
| `id` | INT PK AI | |
| `nama` | VARCHAR | unik |
| `keterangan` | VARCHAR/TEXT | opsional |

### 4.2 `laporan`

Milik petugas (`user_id`). Status verifikasi **dibaca** petugas; **diubah hanya admin** (di luar modul ini).

| Kolom | Tipe (usulan) | Keterangan |
|-------|----------------|------------|
| `id` | INT PK AI | |
| `user_id` | INT FK → `users.id` | petugas pembuat |
| `bencana_id` | INT FK → `bencana.id` | |
| `judul` | VARCHAR | |
| `lokasi` | VARCHAR | |
| `deskripsi` | TEXT | |
| `waktu_kejadian` | DATETIME | |
| `status` | VARCHAR | `DRAFT`, `TERKIRIM`, `DIVERIFIKASI` |
| `created_at` | DATETIME | |
| `updated_at` | DATETIME | |

Aturan bisnis petugas:

- List hanya laporan milik user yang login (jangan tampilkan laporan petugas lain).
- Edit / hapus hanya jika `DRAFT`.
- Aksi **Kirim** mengubah `DRAFT` → `TERKIRIM`.
- `DIVERIFIKASI` tidak diisi oleh petugas.

### 4.3 `dokumentasi`

Foto/file terikat satu laporan.

| Kolom | Tipe (usulan) | Keterangan |
|-------|----------------|------------|
| `id` | INT PK AI | |
| `laporan_id` | INT FK → `laporan.id` | |
| `nama_file` | VARCHAR | nama asli |
| `path_file` | VARCHAR | path simpan di server |
| `uploaded_at` | DATETIME | |

Simpan file di luar Git (misalnya `rewang/uploads/`, pastikan folder itu di `.gitignore`). Validasi tipe (jpg/png/pdf) dan ukuran wajar.

### 4.4 `kebutuhan_bantuan`

Item kebutuhan per laporan (barang/logistik yang diminta petugas). Bukan stok gudang admin.

| Kolom | Tipe (usulan) | Keterangan |
|-------|----------------|------------|
| `id` | INT PK AI | |
| `laporan_id` | INT FK → `laporan.id` | |
| `nama_item` | VARCHAR | |
| `jumlah` | INT | > 0 |
| `satuan` | VARCHAR | misalnya pcs, kg, dus |
| `catatan` | VARCHAR | opsional |

Tambah/ubah/hapus item hanya jika laporan masih `DRAFT` (atau sesuai catatan AI jika disepakati lain — default: hanya `DRAFT`).

### 4.5 `feedback`

Ditulis admin (kelak). Petugas **read-only**.

| Kolom | Tipe (usulan) | Keterangan |
|-------|----------------|------------|
| `id` | INT PK AI | |
| `laporan_id` | INT FK → `laporan.id` | |
| `admin_id` | INT FK → `users.id` | nullable sampai modul admin ada |
| `isi` | TEXT | |
| `created_at` | DATETIME | |

Tanpa form “tulis feedback” di UI petugas. Jika belum ada baris, tampilkan kosong.

Nama tabel/kolom di SQL dan entity harus sama (JPA validate). AI yang mengerjakan P0-02 boleh menyesuaikan nama snake_case asal konsisten dan dicatat di **Catatan AI**.

---

## 5. Perilaku UI petugas (penerimaan)

Semua URL di bawah prefix `/petugas/` agar SecurityConfig yang ada tetap berlaku.

| Halaman | Path usulan | Perilaku |
|---------|-------------|----------|
| Dashboard | `/petugas/dashboard` | Ringkasan jumlah draft/terkirim + menu ke laporan, kebutuhan, dokumentasi, feedback |
| Daftar laporan | `/petugas/laporan` | Hanya milik sendiri |
| Form buat/edit | `/petugas/laporan/baru`, `/petugas/laporan/{id}/edit` | Validasi; edit hanya draft |
| Detail | `/petugas/laporan/{id}` | Status, kebutuhan, dokumentasi, feedback |
| Kirim | POST `/petugas/laporan/{id}/kirim` | Draft → terkirim |
| Kebutuhan | nested di detail/form laporan | CRUD item |
| Dokumentasi | nested di detail laporan | upload + daftar + hapus (draft) |
| Feedback | di detail laporan | daftar baca saja |

Gaya: Bootstrap 5 seperti halaman login/dashboard yang ada. Bahasa UI Indonesia.

Akses: user PETUGAS yang membuka `/admin/**` harus ditolak (403). User tanpa login diarahkan ke `/login`.

---

## 6. Checklist (handoff)

Format tetap. Kerjakan dari atas. Item `[x]` jangan diulang kecuali pemilik minta perbaikan.

### P0 — fondasi

- [x] **P0-01** | Branch kerja petugas | Status: DONE  
  - **File target:** git branch saja (bukan file aplikasi)  
  - **Kriteria selesai:** branch lokal dibuat dari `main` (atau dari titik yang disepakati kelompok) dengan nama yang **kamu** putuskan setelah tanya Alvin.  
  - **Selesai:** 2026-10-19  
  - **Catatan AI:** Nama branch yang diputuskan pemilik: `feature/dokumentasi` (pola `feature/<nama-fitur>`, bukan `feature/petugas`). `main` di-pull dulu (fast-forward 3 commit sidebar/navbar Alvin, PR #2), lalu `git checkout -b feature/dokumentasi` dari `main` terbaru. Belum di-push. Kerja berikutnya: P0-02.

- [ ] **P0-02** | SQL schema tabel petugas + relasi ke `users` | Status: TODO  
  - **File target:** misalnya `docs/sql/petugas-schema.sql` (dan seed `bencana` minimal jika perlu dropdown)  
  - **Kriteria selesai:** skrip CREATE TABLE untuk `bencana`, `laporan`, `dokumentasi`, `kebutuhan_bantuan`, `feedback` dengan FK ke `users`/`bencana`/`laporan`. Bisa dijalankan di MySQL `bantuan_bencana` tanpa error asumsi tabel `users` sudah ada.  
  - **Selesai:** —  
  - **Catatan AI:** `ddl-auto=validate` — jangan mengandalkan Hibernate untuk membuat tabel.

- [ ] **P0-03** | Entity + repository JPA | Status: TODO  
  - **File target:** `rewang/src/main/java/com/example/rewang/entity/*`, `rewang/src/main/java/com/example/rewang/repository/*`  
  - **Kriteria selesai:** entity mapping sesuai P0-02; repository untuk laporan (filter by user), kebutuhan, dokumentasi, feedback, bencana. Aplikasi masih bisa start jika tabel sudah dibuat.  
  - **Selesai:** —  
  - **Catatan AI:** Jangan ubah `User`/`Role` kecuali relasi `@ManyToOne` dari `Laporan` ke `User`.

### P1 — fitur petugas

- [ ] **P1-01** | CRUD + kirim Laporan | Status: TODO  
  - **File target:** controller/service petugas, template `rewang/src/main/resources/templates/petugas/laporan/`  
  - **Kriteria selesai:** petugas bisa buat, lihat daftar milik sendiri, ubah draft, hapus draft, kirim (`DRAFT` → `TERKIRIM`). Validasi field wajib. Tidak bisa edit laporan terkirim.  
  - **Selesai:** —  
  - **Catatan AI:** —

- [ ] **P1-02** | Kebutuhan bantuan per laporan | Status: TODO  
  - **File target:** service/controller/template terkait kebutuhan  
  - **Kriteria selesai:** petugas bisa tambah/ubah/hapus item pada laporan miliknya yang masih draft; item terlihat di detail laporan.  
  - **Selesai:** —  
  - **Catatan AI:** Bukan manajemen stok `Barang` admin.

- [ ] **P1-03** | Upload dokumentasi per laporan | Status: TODO  
  - **File target:** upload handler, template, folder simpan file + `.gitignore`  
  - **Kriteria selesai:** petugas bisa unggah file ke laporan miliknya, melihat daftar, menghapus pada draft. File tidak masuk Git.  
  - **Selesai:** —  
  - **Catatan AI:** —

- [ ] **P1-04** | Lihat feedback admin (read-only) + status laporan | Status: TODO  
  - **File target:** detail laporan / halaman feedback petugas  
  - **Kriteria selesai:** petugas melihat `status` laporan dan daftar feedback tanpa form tulis/ubah. Jika tidak ada feedback, tampilan kosong yang jelas.  
  - **Selesai:** —  
  - **Catatan AI:** Untuk uji, boleh seed 1 baris feedback di SQL lokal — jangan bangun UI admin.

- [ ] **P1-05** | Dashboard petugas | Status: TODO  
  - **File target:** `DashboardController` (method petugas saja), `templates/petugas/dashboard.html`  
  - **Kriteria selesai:** bukan lagi halaman kosong; ada ringkasan (jumlah draft / terkirim) dan menu ke laporan, kebutuhan, dokumentasi, feedback. Logout tetap ada.  
  - **Selesai:** —  
  - **Catatan AI:** Jangan mengubah dashboard admin.

### P2 — penutup tahap ini

- [ ] **P2-01** | Tes compile + batas akses | Status: TODO  
  - **File target:** tes di `rewang/src/test/java/...` jika perlu; tidak mengubah security milik Alvin kecuali bug  
  - **Kriteria selesai:** `mvn test` / compile lolos. PETUGAS tidak bisa akses `/admin/**`. Halaman petugas butuh login PETUGAS.  
  - **Selesai:** —  
  - **Catatan AI:** Pemilik tetap wajib tes manual di browser (H-04).

- [ ] **P2-02** | Chat AI | Status: DITUNDA  
  - **File target:** —  
  - **Kriteria selesai:** tidak dikerjakan di tahap ini.  
  - **Selesai:** —  
  - **Catatan AI:** Jangan mulai. Butuh keputusan API key dan desain chat terpisah.

---

## 7. Protokol jika sesi limit / ganti AI

```text
Baca PRD → ambil TODO pertama → kerjakan 1 tugas → update checklist
→ jika sesi masih cukup, lanjut TODO berikutnya
→ jika limit, berhenti setelah checklist ter-update; manusia tempel prompt di bagian 0 ke AI baru
```

Aturan berhenti yang rapi:

- Jangan tinggalkan compile rusak di tengah tugas. Jika tidak selesai, biarkan item `[ ]` dan tulis di **Catatan AI** apa yang sudah tersentuh.
- Jangan mulai P1 sebelum P0-02 dan P0-03 selesai (entity harus cocok dengan tabel).
- P0-01 selesai: branch lokal `feature/dokumentasi`.

---

## 8. Log handoff singkat

Isi baris baru setiap kali AI selesai (atau terhenti).

| Waktu | AI / sesi | Item | Hasil |
|-------|-----------|------|--------|
| 2026-10-19 | inisialisasi PRD | — | File `docs/PRD-PETUGAS.md` dibuat. Belum ada implementasi fitur. Item pertama yang menunggu manusia: **P0-01 nama branch**. |
| 2026-10-19 | sesi branch dokumentasi | P0-01 | `git pull` di `main` (sidebar/navbar), lalu branch lokal `feature/dokumentasi`. Belum push/commit. TODO berikutnya: P0-02. |

Langkah berikutnya: minta AI mengerjakan **P0-02** (SQL schema tabel petugas).

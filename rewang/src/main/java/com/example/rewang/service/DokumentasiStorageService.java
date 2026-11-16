package com.example.rewang.service;

import com.example.rewang.entity.Dokumentasi;
import com.example.rewang.entity.Laporan;
import com.example.rewang.entity.User;
import com.example.rewang.repository.DokumentasiRepository;
import com.example.rewang.repository.LaporanRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DokumentasiStorageService {

    public static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "pdf");

    private final DokumentasiRepository dokumentasiRepository;
    private final LaporanRepository laporanRepository;
    private final Path uploadRoot;

    public DokumentasiStorageService(
            DokumentasiRepository dokumentasiRepository,
            LaporanRepository laporanRepository,
            @Value("${rewang.upload-dir:uploads}") String uploadDir) {
        this.dokumentasiRepository = dokumentasiRepository;
        this.laporanRepository = laporanRepository;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public Optional<Laporan> laporanMilik(User user, Integer laporanId) {
        return laporanRepository.findByIdAndUserId(laporanId, user.getId());
    }

    public List<Dokumentasi> daftar(Integer laporanId) {
        return dokumentasiRepository.findByLaporanIdOrderByCreatedAtDesc(laporanId);
    }

    public long jumlah(Integer laporanId) {
        return dokumentasiRepository.countByLaporanId(laporanId);
    }

    @Transactional
    public Dokumentasi unggah(Laporan laporan, User user, MultipartFile file) throws IOException {
        return unggah(laporan, user, file, null);
    }

    @Transactional
    public Dokumentasi unggah(Laporan laporan, User user, MultipartFile file, String keterangan) throws IOException {
        if (!laporan.isDraft()) {
            throw new IllegalStateException("Dokumentasi hanya bisa diunggah pada laporan draft.");
        }
        validasiFile(file);

        String original = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
        String ext = extension(original);
        String storedName = UUID.randomUUID() + "." + ext;
        Path relative = Paths.get("dokumentasi", String.valueOf(laporan.getId()), storedName);
        Path target = uploadRoot.resolve(relative).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Path file tidak valid.");
        }
        Files.createDirectories(target.getParent());
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        String cleanName = original.replace("\\", "/");
        if (cleanName.contains("/")) {
            cleanName = cleanName.substring(cleanName.lastIndexOf('/') + 1);
        }
        String relativeUrl = relative.toString().replace('\\', '/');

        Dokumentasi row = new Dokumentasi();
        row.setLaporan(laporan);
        row.setUploadedBy(user);
        row.setFileUrl(relativeUrl);
        row.setJenisFile(jenisFileFromExt(ext));
        row.setKeterangan(keterangan);
        row.setCreatedAt(LocalDateTime.now());
        // kolom lama tetap diisi agar backward-compatible
        row.setNamaFile(cleanName);
        row.setPathFile(relativeUrl);
        row.setUploadedAt(row.getCreatedAt());
        return dokumentasiRepository.save(row);
    }

    /** Map extension ke jenis_file sesuai ERD */
    private static String jenisFileFromExt(String ext) {
        return switch (ext) {
            case "pdf" -> "dokumen";
            default -> "foto";
        };
    }

    @Transactional
    public void hapus(User user, Integer laporanId, Integer dokumentasiId) throws IOException {
        Laporan laporan = laporanMilik(user, laporanId)
                .orElseThrow(() -> new IllegalArgumentException("Laporan tidak ditemukan."));
        if (!laporan.isDraft()) {
            throw new IllegalStateException("Dokumentasi hanya bisa dihapus pada laporan draft.");
        }
        Dokumentasi row = dokumentasiRepository.findByIdAndLaporanId(dokumentasiId, laporanId)
                .orElseThrow(() -> new IllegalArgumentException("File tidak ditemukan."));
        Path target = uploadRoot.resolve(row.getFileUrl()).normalize();
        dokumentasiRepository.delete(row);
        if (target.startsWith(uploadRoot)) {
            Files.deleteIfExists(target);
        }
    }

    @Transactional
    public void hapusSemuaDokumentasiLaporan(User user, Integer laporanId) throws IOException {
        Laporan laporan = laporanMilik(user, laporanId)
                .orElseThrow(() -> new IllegalArgumentException("Laporan tidak ditemukan."));
        if (!laporan.isDraft()) {
            throw new IllegalStateException("Hanya bisa menghapus dokumentasi pada laporan draft.");
        }
        List<Dokumentasi> list = dokumentasiRepository.findByLaporanIdOrderByCreatedAtDesc(laporanId);
        for (Dokumentasi dok : list) {
            Path target = uploadRoot.resolve(dok.getFileUrl()).normalize();
            dokumentasiRepository.delete(dok);
            if (target.startsWith(uploadRoot)) {
                Files.deleteIfExists(target);
            }
        }
    }

    public Resource unduh(User user, Integer laporanId, Integer dokumentasiId) {
        laporanMilik(user, laporanId)
                .orElseThrow(() -> new IllegalArgumentException("Laporan tidak ditemukan."));
        Dokumentasi row = dokumentasiRepository.findByIdAndLaporanId(dokumentasiId, laporanId)
                .orElseThrow(() -> new IllegalArgumentException("File tidak ditemukan."));
        Path target = uploadRoot.resolve(row.getFileUrl()).normalize();
        if (!target.startsWith(uploadRoot) || !Files.isRegularFile(target)) {
            throw new IllegalArgumentException("File tidak ditemukan di server.");
        }
        return new FileSystemResource(target);
    }

    public MediaType mediaType(String namaFile) {
        String ext = extension(namaFile);
        return switch (ext) {
            case "png" -> MediaType.IMAGE_PNG;
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "pdf" -> MediaType.APPLICATION_PDF;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }

    public static void validasiFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Pilih file jpg, png, atau pdf.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("Ukuran file maksimal 5 MB.");
        }
        String ext = extension(Optional.ofNullable(file.getOriginalFilename()).orElse(""));
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("Tipe file harus jpg, png, atau pdf.");
        }
    }

    static String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}

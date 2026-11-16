package com.example.rewang.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "dokumentasi")
public class Dokumentasi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "laporan_id")
    private Laporan laporan;

    @ManyToOne
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "jenis_file", nullable = false, length = 30)
    private String jenisFile = "foto";

    @Column(name = "keterangan")
    private String keterangan;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // kolom lama tetap di-map agar data lama masih terbaca
    @Column(name = "nama_file")
    private String namaFile;

    @Column(name = "path_file")
    private String pathFile;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    /** Nama tampilan: ambil dari namaFile jika ada, fallback ke basename fileUrl */
    public String getDisplayName() {
        if (namaFile != null && !namaFile.isBlank()) return namaFile;
        if (fileUrl == null) return "file";
        int slash = fileUrl.lastIndexOf('/');
        return slash >= 0 ? fileUrl.substring(slash + 1) : fileUrl;
    }
}

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
@Table(name = "laporan")
public class Laporan {

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_TERKIRIM = "TERKIRIM";
    public static final String STATUS_DIVERIFIKASI = "DIVERIFIKASI";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "bencana_id")
    private Bencana bencana;

    private String judul;

    private String lokasi;

    private String deskripsi;

    @Column(name = "waktu_kejadian")
    private LocalDateTime waktuKejadian;

    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isDraft() {
        return STATUS_DRAFT.equals(status);
    }
}

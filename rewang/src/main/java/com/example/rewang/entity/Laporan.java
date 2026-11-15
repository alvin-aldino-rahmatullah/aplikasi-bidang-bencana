
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

    public static final String STATUS_DRAFT = "draft";
    public static final String STATUS_TERKIRIM = "diajukan";
    public static final String STATUS_DIVERIFIKASI = "diverifikasi";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "bencana_id", nullable = false)
    private Bencana bencana;

    @Column(name = "verified_by")
    private Integer verifiedBy;

    @Column(name = "judul", nullable = false, length = 200)
    private String judul;

    @Column(name = "isi", nullable = false, columnDefinition = "TEXT")
    private String isi;

    @Column(name = "status", nullable = false)
    private String status = STATUS_DRAFT;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isDraft() {
        return STATUS_DRAFT.equals(status);
    }
}

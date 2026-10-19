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

    @Column(name = "nama_file")
    private String namaFile;

    @Column(name = "path_file")
    private String pathFile;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
}

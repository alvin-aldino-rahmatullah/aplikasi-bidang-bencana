package com.example.rewang.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bencana")
public class Bencana {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // kolom di database: nama_bencana (field tetap "nama" supaya getNama() di kode lain tidak berubah)
    @Column(name = "nama_bencana")
    private String nama;

    private String jenis;

    private String lokasi;

    @Column(name = "tanggal_kejadian")
    private LocalDate tanggalKejadian;
}

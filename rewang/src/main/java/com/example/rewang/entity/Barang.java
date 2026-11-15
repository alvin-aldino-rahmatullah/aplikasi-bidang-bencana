
package com.example.rewang.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "barang")
public class Barang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nama_barang", nullable = false, length = 150)
    private String namaBarang;

    @Column(name = "satuan", nullable = false, length = 30)
    private String satuan;

    @Column(name = "stok", nullable = false)
    private Integer stok;

    @Column(name = "stok_minimum", nullable = false)
    private Integer stokMinimum;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

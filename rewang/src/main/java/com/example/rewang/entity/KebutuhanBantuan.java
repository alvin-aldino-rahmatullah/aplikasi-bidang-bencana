
package com.example.rewang.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "kebutuhan_bantuan")
public class KebutuhanBantuan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "laporan_id", nullable = false)
    private Laporan laporan;

    @ManyToOne
    @JoinColumn(name = "barang_id", nullable = false)
    private Barang barang;

    @Column(name = "jumlah_diminta", nullable = false)
    private Integer jumlahDiminta;

    @Column(name = "jumlah_disetujui", nullable = false)
    private Integer jumlahDisetujui = 0;

    @Column(name = "status", nullable = false)
    private String status = "menunggu";
}

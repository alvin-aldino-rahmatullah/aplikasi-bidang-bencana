package com.example.rewang.dto;

import java.util.List;

public record StatistikResponse(
        Ringkasan ringkasan,
        List<Tren> tren,
        List<Jenis> komposisi,
        List<Capaian> wilayah,
        List<Capaian> barang) {

    public record Ringkasan(long disetujui, long diminta, double rataVerifikasiJam, long stokMenipis) {}

    public record Tren(String pekan, long diminta, long disetujui) {}

    public record Jenis(String jenis, long jumlah) {}

    // dipakai untuk tabel per wilayah dan progress per barang
    public record Capaian(String nama, long diminta, long disetujui) {}

    public record Pilihan(Long id, String nama) {}

    public record Opsi(List<String> wilayah, List<Pilihan> bencana) {}
}

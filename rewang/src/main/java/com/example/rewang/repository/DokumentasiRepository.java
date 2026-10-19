package com.example.rewang.repository;

import com.example.rewang.entity.Dokumentasi;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DokumentasiRepository extends JpaRepository<Dokumentasi, Integer> {
    List<Dokumentasi> findByLaporanIdOrderByUploadedAtDesc(Integer laporanId);

    Optional<Dokumentasi> findByIdAndLaporanId(Integer id, Integer laporanId);

    long countByLaporanId(Integer laporanId);
}

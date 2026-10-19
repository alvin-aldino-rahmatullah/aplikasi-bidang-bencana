package com.example.rewang.repository;

import com.example.rewang.entity.Laporan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaporanRepository extends JpaRepository<Laporan, Integer> {
    List<Laporan> findByUserIdOrderByUpdatedAtDesc(Integer userId);

    Optional<Laporan> findByIdAndUserId(Integer id, Integer userId);
}

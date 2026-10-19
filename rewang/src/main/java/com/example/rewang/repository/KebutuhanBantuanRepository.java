package com.example.rewang.repository;

import com.example.rewang.entity.KebutuhanBantuan;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KebutuhanBantuanRepository extends JpaRepository<KebutuhanBantuan, Integer> {
    List<KebutuhanBantuan> findByLaporanId(Integer laporanId);
}

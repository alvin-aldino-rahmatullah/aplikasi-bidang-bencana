package com.example.rewang.repository;

import com.example.rewang.entity.Bencana;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BencanaRepository extends JpaRepository<Bencana, Integer> {
    List<Bencana> findAllByOrderByNamaAsc();
}

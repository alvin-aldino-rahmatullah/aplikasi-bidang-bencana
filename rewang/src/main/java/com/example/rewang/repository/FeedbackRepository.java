package com.example.rewang.repository;

import com.example.rewang.entity.Feedback;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    List<Feedback> findByLaporanIdOrderByCreatedAtDesc(Integer laporanId);
}

package com.example.rewang.controller;

import com.example.rewang.entity.Feedback;
import com.example.rewang.entity.Laporan;
import com.example.rewang.entity.User;
import com.example.rewang.repository.FeedbackRepository;
import com.example.rewang.repository.LaporanRepository;
import com.example.rewang.service.CurrentUserService;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/petugas/feedback")
public class FeedbackController {

    private final CurrentUserService currentUserService;
    private final FeedbackRepository feedbackRepository;
    private final LaporanRepository laporanRepository;

    public FeedbackController(
            CurrentUserService currentUserService,
            FeedbackRepository feedbackRepository,
            LaporanRepository laporanRepository) {
        this.currentUserService = currentUserService;
        this.feedbackRepository = feedbackRepository;
        this.laporanRepository = laporanRepository;
    }

    @GetMapping
    public String daftar(
            Authentication authentication,
            @RequestParam(required = false) Integer laporanId,
            Model model) {
        User user = currentUserService.requireUser(authentication);

        List<Feedback> feedbackList;
        Laporan selectedLaporan = null;

        if (laporanId != null) {
            selectedLaporan = laporanRepository.findByIdAndUserId(laporanId, user.getId()).orElse(null);
            if (selectedLaporan != null) {
                feedbackList = feedbackRepository.findByLaporanIdOrderByCreatedAtDesc(laporanId);
            } else {
                feedbackList = List.of();
            }
        } else {
            feedbackList = feedbackRepository.findByLaporanUserIdOrderByCreatedAtDesc(user.getId());
        }

        model.addAttribute("feedbackList", feedbackList);
        model.addAttribute("selectedLaporan", selectedLaporan);
        return "petugas/feedback/daftar";
    }
}


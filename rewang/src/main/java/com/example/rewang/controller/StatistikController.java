package com.example.rewang.controller;

import com.example.rewang.dto.StatistikResponse;
import com.example.rewang.repository.StatistikRepository;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class StatistikController {

    private final StatistikRepository repo;

    public StatistikController(StatistikRepository repo) {
        this.repo = repo;
    }

    // halaman: templates/admin/statistik.html
    @GetMapping("/admin/statistik")
    public String halaman() {
        return "admin/statistik";
    }

    @GetMapping("/api/statistik")
    @ResponseBody
    public StatistikResponse statistik(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) String wilayah,
            @RequestParam(required = false) Long bencanaId) {

        return new StatistikResponse(
                repo.ringkasan(start, end, wilayah, bencanaId),
                repo.tren(start, end, wilayah, bencanaId),
                repo.komposisi(start, end, wilayah, bencanaId),
                repo.perWilayah(start, end, wilayah, bencanaId),
                repo.perBarang(start, end, wilayah, bencanaId));
    }

    @GetMapping("/api/statistik/opsi")
    @ResponseBody
    public StatistikResponse.Opsi opsi() {
        return repo.opsi();
    }
}

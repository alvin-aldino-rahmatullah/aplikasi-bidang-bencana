package com.example.rewang.controller;

import com.example.rewang.entity.Bencana;
import com.example.rewang.entity.Laporan;
import com.example.rewang.entity.User;
import com.example.rewang.repository.BencanaRepository;
import com.example.rewang.repository.LaporanRepository;
import com.example.rewang.service.CurrentUserService;
import com.example.rewang.service.DokumentasiStorageService;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/petugas/dokumentasi")
public class DokumentasiController {

    private final CurrentUserService currentUserService;
    private final LaporanRepository laporanRepository;
    private final BencanaRepository bencanaRepository;
    private final DokumentasiStorageService dokumentasiStorageService;

    public DokumentasiController(
            CurrentUserService currentUserService,
            LaporanRepository laporanRepository,
            BencanaRepository bencanaRepository,
            DokumentasiStorageService dokumentasiStorageService) {
        this.currentUserService = currentUserService;
        this.laporanRepository = laporanRepository;
        this.bencanaRepository = bencanaRepository;
        this.dokumentasiStorageService = dokumentasiStorageService;
    }

    @GetMapping
    public String daftar(Authentication authentication, Model model) {
        User user = currentUserService.requireUser(authentication);
        List<Laporan> laporanList = laporanRepository.findByUserIdOrderByUpdatedAtDesc(user.getId());
        Map<Integer, Long> jumlahFile = new LinkedHashMap<>();
        for (Laporan laporan : laporanList) {
            jumlahFile.put(laporan.getId(), dokumentasiStorageService.jumlah(laporan.getId()));
        }
        model.addAttribute("laporanList", laporanList);
        model.addAttribute("jumlahFile", jumlahFile);
        model.addAttribute("bencanaList", bencanaRepository.findAllByOrderByNamaAsc());
        return "petugas/dokumentasi/daftar";
    }

    @PostMapping("/laporan-draft")
    public String buatDraft(
            Authentication authentication,
            @RequestParam Integer bencanaId,
            @RequestParam String judul,
            @RequestParam String lokasi,
            @RequestParam String waktuKejadian,
            @RequestParam(required = false) String deskripsi,
            RedirectAttributes redirectAttributes) {
        User user = currentUserService.requireUser(authentication);
        if (judul == null || judul.isBlank() || lokasi == null || lokasi.isBlank() || waktuKejadian == null || waktuKejadian.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Judul, lokasi, dan waktu kejadian wajib diisi.");
            return "redirect:/petugas/dokumentasi";
        }
        Bencana bencana = bencanaRepository.findById(bencanaId).orElse(null);
        if (bencana == null) {
            redirectAttributes.addFlashAttribute("error", "Jenis bencana tidak valid.");
            return "redirect:/petugas/dokumentasi";
        }
        LocalDateTime now = LocalDateTime.now();
        Laporan laporan = new Laporan();
        laporan.setUser(user);
        laporan.setBencana(bencana);
        laporan.setJudul(judul.trim());
        laporan.setLokasi(lokasi.trim());
        laporan.setDeskripsi(deskripsi == null ? null : deskripsi.trim());
        try {
            laporan.setWaktuKejadian(LocalDateTime.parse(waktuKejadian));
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Format waktu kejadian tidak valid.");
            return "redirect:/petugas/dokumentasi";
        }
        laporan.setStatus(Laporan.STATUS_DRAFT);
        laporan.setCreatedAt(now);
        laporan.setUpdatedAt(now);
        laporan = laporanRepository.save(laporan);
        redirectAttributes.addFlashAttribute("sukses", "Laporan draft dibuat. Unggah dokumentasi di halaman ini.");
        return "redirect:/petugas/dokumentasi/" + laporan.getId();
    }

    @GetMapping("/{laporanId}")
    public String detail(
            Authentication authentication,
            @PathVariable Integer laporanId,
            Model model,
            RedirectAttributes redirectAttributes) {
        User user = currentUserService.requireUser(authentication);
        Laporan laporan = dokumentasiStorageService.laporanMilik(user, laporanId).orElse(null);
        if (laporan == null) {
            redirectAttributes.addFlashAttribute("error", "Laporan tidak ditemukan atau bukan milik Anda.");
            return "redirect:/petugas/dokumentasi";
        }
        model.addAttribute("laporan", laporan);
        model.addAttribute("berkas", dokumentasiStorageService.daftar(laporanId));
        return "petugas/dokumentasi/detail";
    }

    @PostMapping("/{laporanId}")
    public String unggah(
            Authentication authentication,
            @PathVariable Integer laporanId,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        User user = currentUserService.requireUser(authentication);
        Laporan laporan = dokumentasiStorageService.laporanMilik(user, laporanId).orElse(null);
        if (laporan == null) {
            redirectAttributes.addFlashAttribute("error", "Laporan tidak ditemukan atau bukan milik Anda.");
            return "redirect:/petugas/dokumentasi";
        }
        try {
            dokumentasiStorageService.unggah(laporan, file);
            redirectAttributes.addFlashAttribute("sukses", "File berhasil diunggah.");
        } catch (IllegalArgumentException | IllegalStateException | IOException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/petugas/dokumentasi/" + laporanId;
    }

    @PostMapping("/{laporanId}/{dokId}/hapus")
    public String hapus(
            Authentication authentication,
            @PathVariable Integer laporanId,
            @PathVariable Integer dokId,
            RedirectAttributes redirectAttributes) {
        User user = currentUserService.requireUser(authentication);
        try {
            dokumentasiStorageService.hapus(user, laporanId, dokId);
            redirectAttributes.addFlashAttribute("sukses", "File dihapus.");
        } catch (IllegalArgumentException | IllegalStateException | IOException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/petugas/dokumentasi/" + laporanId;
    }

    @GetMapping("/{laporanId}/{dokId}/unduh")
    public ResponseEntity<Resource> unduh(
            Authentication authentication,
            @PathVariable Integer laporanId,
            @PathVariable Integer dokId) {
        User user = currentUserService.requireUser(authentication);
        var dokumen = dokumentasiStorageService.daftar(laporanId).stream()
                .filter(d -> d.getId().equals(dokId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("File tidak ditemukan."));
        Resource resource = dokumentasiStorageService.unduh(user, laporanId, dokId);
        String encoded = URLEncoder.encode(dokumen.getNamaFile(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encoded)
                .contentType(dokumentasiStorageService.mediaType(dokumen.getNamaFile()))
                .body(resource);
    }
}

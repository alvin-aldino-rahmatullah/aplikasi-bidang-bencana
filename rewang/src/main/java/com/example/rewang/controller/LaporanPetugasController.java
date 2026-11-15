package com.example.rewang.controller;

import com.example.rewang.entity.Bencana;
import com.example.rewang.entity.Dokumentasi;
import com.example.rewang.entity.Feedback;
import com.example.rewang.entity.Laporan;
import com.example.rewang.entity.User;
import com.example.rewang.repository.BencanaRepository;
import com.example.rewang.repository.DokumentasiRepository;
import com.example.rewang.repository.FeedbackRepository;
import com.example.rewang.repository.LaporanRepository;
import com.example.rewang.service.CurrentUserService;
import com.example.rewang.service.DokumentasiStorageService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/petugas/laporan")
public class LaporanPetugasController {

    private final CurrentUserService currentUserService;
    private final LaporanRepository laporanRepository;
    private final BencanaRepository bencanaRepository;
    private final DokumentasiRepository dokumentasiRepository;
    private final FeedbackRepository feedbackRepository;
    private final DokumentasiStorageService dokumentasiStorageService;

    public LaporanPetugasController(
            CurrentUserService currentUserService,
            LaporanRepository laporanRepository,
            BencanaRepository bencanaRepository,
            DokumentasiRepository dokumentasiRepository,
            FeedbackRepository feedbackRepository,
            DokumentasiStorageService dokumentasiStorageService) {
        this.currentUserService = currentUserService;
        this.laporanRepository = laporanRepository;
        this.bencanaRepository = bencanaRepository;
        this.dokumentasiRepository = dokumentasiRepository;
        this.feedbackRepository = feedbackRepository;
        this.dokumentasiStorageService = dokumentasiStorageService;
    }

    // Menampilkan daftar laporan bencana milik petugas yang login
    @GetMapping
    public String daftar(
            Authentication authentication,
            @RequestParam(required = false) String status,
            Model model) {

        User user = currentUserService.requireUser(authentication);

        List<Laporan> laporanList;
        if (status != null && !status.isBlank()) {
            laporanList = laporanRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(
                    user.getId(), status.trim().toLowerCase());
        } else {
            laporanList = laporanRepository.findByUserIdOrderByUpdatedAtDesc(user.getId());
        }

        Map<Integer, Long> jumlahDokumentasi = new LinkedHashMap<>();
        for (Laporan lap : laporanList) {
            jumlahDokumentasi.put(lap.getId(), dokumentasiRepository.countByLaporanId(lap.getId()));
        }

        model.addAttribute("laporanList", laporanList);
        model.addAttribute("jumlahDokumentasi", jumlahDokumentasi);
        model.addAttribute("selectedStatus", status);

        return "petugas/laporan/daftar";
    }

    // Menampilkan form tambah laporan bencana baru
    @GetMapping("/buat")
    public String buatForm(Model model) {
        model.addAttribute("bencanaList", bencanaRepository.findAllByOrderByNamaAsc());
        return "petugas/laporan/buat";
    }

    // Menyimpan laporan bencana baru
    @PostMapping("/buat")
    public String simpan(
            Authentication authentication,
            @RequestParam(required = false) Integer bencanaId,
            @RequestParam(required = false) String judul,
            @RequestParam(required = false) String isi,
            @RequestParam(defaultValue = "draft") String aksi,
            RedirectAttributes redirectAttributes) {

        User user = currentUserService.requireUser(authentication);

        if (bencanaId == null || judul == null || judul.isBlank() || isi == null || isi.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Jenis bencana, judul, dan isi laporan wajib diisi.");
            return "redirect:/petugas/laporan/buat";
        }

        if (judul.trim().length() > 200) {
            redirectAttributes.addFlashAttribute("error", "Judul laporan tidak boleh lebih dari 200 karakter.");
            return "redirect:/petugas/laporan/buat";
        }

        Bencana bencana = bencanaRepository.findById(bencanaId).orElse(null);
        if (bencana == null) {
            redirectAttributes.addFlashAttribute("error", "Data bencana tidak ditemukan.");
            return "redirect:/petugas/laporan/buat";
        }

        LocalDateTime now = LocalDateTime.now();
        boolean ajukan = "ajukan".equalsIgnoreCase(aksi);

        Laporan laporan = new Laporan();
        laporan.setUser(user);
        laporan.setBencana(bencana);
        laporan.setJudul(judul.trim());
        laporan.setIsi(isi.trim());
        laporan.setStatus(ajukan ? Laporan.STATUS_TERKIRIM : Laporan.STATUS_DRAFT);
        laporan.setCreatedAt(now);
        laporan.setUpdatedAt(now);

        laporan = laporanRepository.save(laporan);

        if (ajukan) {
            redirectAttributes.addFlashAttribute("sukses", "Laporan bencana berhasil dibuat dan diajukan ke Admin.");
        } else {
            redirectAttributes.addFlashAttribute("sukses", "Laporan bencana berhasil disimpan sebagai draft.");
        }

        return "redirect:/petugas/laporan/" + laporan.getId();
    }

    // Menampilkan detail laporan bencana
    @GetMapping("/{id}")
    public String detail(
            Authentication authentication,
            @PathVariable Integer id,
            Model model,
            RedirectAttributes redirectAttributes) {

        User user = currentUserService.requireUser(authentication);

        Laporan laporan = laporanRepository.findByIdAndUserId(id, user.getId()).orElse(null);
        if (laporan == null) {
            redirectAttributes.addFlashAttribute("error", "Laporan tidak ditemukan atau bukan milik Anda.");
            return "redirect:/petugas/laporan";
        }

        List<Dokumentasi> berkasList = dokumentasiRepository.findByLaporanIdOrderByUploadedAtDesc(id);
        List<Feedback> feedbackList = feedbackRepository.findByLaporanIdOrderByCreatedAtDesc(id);

        model.addAttribute("laporan", laporan);
        model.addAttribute("berkasList", berkasList);
        model.addAttribute("feedbackList", feedbackList);

        return "petugas/laporan/detail";
    }

    // Menampilkan form edit laporan bencana draft
    @GetMapping("/{id}/edit")
    public String editForm(
            Authentication authentication,
            @PathVariable Integer id,
            Model model,
            RedirectAttributes redirectAttributes) {

        User user = currentUserService.requireUser(authentication);

        Laporan laporan = laporanRepository.findByIdAndUserId(id, user.getId()).orElse(null);
        if (laporan == null) {
            redirectAttributes.addFlashAttribute("error", "Laporan tidak ditemukan atau bukan milik Anda.");
            return "redirect:/petugas/laporan";
        }

        if (!laporan.isDraft()) {
            redirectAttributes.addFlashAttribute("error", "Hanya laporan berstatus draft yang dapat diedit.");
            return "redirect:/petugas/laporan/" + id;
        }

        model.addAttribute("laporan", laporan);
        model.addAttribute("bencanaList", bencanaRepository.findAllByOrderByNamaAsc());

        return "petugas/laporan/edit";
    }

    // Menyimpan pembaruan laporan draft
    @PostMapping("/{id}/edit")
    public String update(
            Authentication authentication,
            @PathVariable Integer id,
            @RequestParam(required = false) Integer bencanaId,
            @RequestParam(required = false) String judul,
            @RequestParam(required = false) String isi,
            @RequestParam(defaultValue = "draft") String aksi,
            RedirectAttributes redirectAttributes) {

        User user = currentUserService.requireUser(authentication);

        Laporan laporan = laporanRepository.findByIdAndUserId(id, user.getId()).orElse(null);
        if (laporan == null) {
            redirectAttributes.addFlashAttribute("error", "Laporan tidak ditemukan atau bukan milik Anda.");
            return "redirect:/petugas/laporan";
        }

        if (!laporan.isDraft()) {
            redirectAttributes.addFlashAttribute("error", "Hanya laporan berstatus draft yang dapat diedit.");
            return "redirect:/petugas/laporan/" + id;
        }

        if (bencanaId == null || judul == null || judul.isBlank() || isi == null || isi.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Jenis bencana, judul, dan isi laporan wajib diisi.");
            return "redirect:/petugas/laporan/" + id + "/edit";
        }

        if (judul.trim().length() > 200) {
            redirectAttributes.addFlashAttribute("error", "Judul laporan tidak boleh lebih dari 200 karakter.");
            return "redirect:/petugas/laporan/" + id + "/edit";
        }

        Bencana bencana = bencanaRepository.findById(bencanaId).orElse(null);
        if (bencana == null) {
            redirectAttributes.addFlashAttribute("error", "Data bencana tidak ditemukan.");
            return "redirect:/petugas/laporan/" + id + "/edit";
        }

        boolean ajukan = "ajukan".equalsIgnoreCase(aksi);

        laporan.setBencana(bencana);
        laporan.setJudul(judul.trim());
        laporan.setIsi(isi.trim());
        if (ajukan) {
            laporan.setStatus(Laporan.STATUS_TERKIRIM);
        }
        laporan.setUpdatedAt(LocalDateTime.now());

        laporanRepository.save(laporan);

        if (ajukan) {
            redirectAttributes.addFlashAttribute("sukses", "Laporan bencana berhasil diperbarui dan diajukan ke Admin.");
        } else {
            redirectAttributes.addFlashAttribute("sukses", "Laporan draft berhasil diperbarui.");
        }

        return "redirect:/petugas/laporan/" + id;
    }

    // Mengajukan laporan draft ke admin
    @PostMapping("/{id}/ajukan")
    public String ajukan(
            Authentication authentication,
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {

        User user = currentUserService.requireUser(authentication);

        Laporan laporan = laporanRepository.findByIdAndUserId(id, user.getId()).orElse(null);
        if (laporan == null) {
            redirectAttributes.addFlashAttribute("error", "Laporan tidak ditemukan atau bukan milik Anda.");
            return "redirect:/petugas/laporan";
        }

        if (!laporan.isDraft()) {
            redirectAttributes.addFlashAttribute("error", "Laporan ini sudah diajukan sebelumnya.");
            return "redirect:/petugas/laporan/" + id;
        }

        laporan.setStatus(Laporan.STATUS_TERKIRIM);
        laporan.setUpdatedAt(LocalDateTime.now());
        laporanRepository.save(laporan);

        redirectAttributes.addFlashAttribute("sukses", "Laporan berhasil diajukan ke Admin untuk verifikasi.");
        return "redirect:/petugas/laporan/" + id;
    }

    // Menghapus laporan draft
    @PostMapping("/{id}/hapus")
    public String hapus(
            Authentication authentication,
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {

        User user = currentUserService.requireUser(authentication);

        Laporan laporan = laporanRepository.findByIdAndUserId(id, user.getId()).orElse(null);
        if (laporan == null) {
            redirectAttributes.addFlashAttribute("error", "Laporan tidak ditemukan atau bukan milik Anda.");
            return "redirect:/petugas/laporan";
        }

        if (!laporan.isDraft()) {
            redirectAttributes.addFlashAttribute("error", "Hanya laporan berstatus draft yang dapat dihapus.");
            return "redirect:/petugas/laporan/" + id;
        }

        try {
            dokumentasiStorageService.hapusSemuaDokumentasiLaporan(user, id);
        } catch (IOException e) {
            // log and continue
        }

        laporanRepository.delete(laporan);

        redirectAttributes.addFlashAttribute("sukses", "Laporan draft berhasil dihapus.");
        return "redirect:/petugas/laporan";
    }
}

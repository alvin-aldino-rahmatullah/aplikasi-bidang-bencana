
package com.example.rewang.controller;

import com.example.rewang.entity.Barang;
import com.example.rewang.service.BarangService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/barang")
public class BarangController {

    private final BarangService barangService;

    public BarangController(BarangService barangService) {
        this.barangService = barangService;
    }

    // Menampilkan daftar stok barang
    @GetMapping
    public String index(Model model) {
        model.addAttribute("daftarBarang",
                barangService.getSemuaBarang());
        return "admin/barang/index";
    }

    // Menampilkan halaman tambah barang
    @GetMapping("/tambah")
    public String tambah(Model model) {
        model.addAttribute("barang", new Barang());
        return "admin/barang/form";
    }

    // Menyimpan barang baru
    @PostMapping("/simpan")
    public String simpan(
            @ModelAttribute Barang barang,
            RedirectAttributes redirectAttributes) {

        barangService.tambahBarang(barang);
        redirectAttributes.addFlashAttribute(
                "success", "Barang berhasil ditambahkan");

        return "redirect:/admin/barang";
    }

    // Menampilkan halaman edit barang
    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Integer id,
            Model model) {

        model.addAttribute(
                "barang", barangService.getBarangById(id));

        return "admin/barang/form";
    }

    // Memperbarui data barang
    @PostMapping("/update/{id}")
    public String update(
            @PathVariable Integer id,
            @ModelAttribute Barang barang,
            RedirectAttributes redirectAttributes) {

        barangService.updateBarang(id, barang);

        redirectAttributes.addFlashAttribute(
                "success", "Barang berhasil diperbarui");

        return "redirect:/admin/barang";
    }

    // Menghapus barang
    @PostMapping("/hapus/{id}")
    public String hapus(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {

        barangService.hapusBarang(id);

        redirectAttributes.addFlashAttribute(
                "success", "Barang berhasil dihapus");

        return "redirect:/admin/barang";
    }
}

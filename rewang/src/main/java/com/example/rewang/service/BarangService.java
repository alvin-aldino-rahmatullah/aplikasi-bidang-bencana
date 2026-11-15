
package com.example.rewang.service;

import com.example.rewang.entity.Barang;
import com.example.rewang.repository.BarangRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BarangService {

    private final BarangRepository barangRepository;

    public BarangService(BarangRepository barangRepository) {
        this.barangRepository = barangRepository;
    }

    public List<Barang> getSemuaBarang() {
        return barangRepository.findAll();
    }

    public Barang getBarangById(Integer id) {
        return barangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Barang tidak ditemukan"));
    }

    public Barang tambahBarang(Barang barang) {
        return barangRepository.save(barang);
    }

    public Barang updateBarang(Integer id, Barang barangBaru) {
        Barang barang = getBarangById(id);

        barang.setNamaBarang(barangBaru.getNamaBarang());
        barang.setSatuan(barangBaru.getSatuan());
        barang.setStok(barangBaru.getStok());
        barang.setStokMinimum(barangBaru.getStokMinimum());

        return barangRepository.save(barang);
    }

    public void hapusBarang(Integer id) {
        barangRepository.deleteById(id);
    }
}

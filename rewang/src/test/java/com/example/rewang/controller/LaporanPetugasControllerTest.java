package com.example.rewang.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class LaporanPetugasControllerTest {

    private User user;
    private CurrentUserService currentUserService;
    private Authentication auth;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setNama("Petugas Alpha");
        user.setEmail("petugas@test.com");

        currentUserService = new CurrentUserService(null) {
            @Override
            public User requireUser(Authentication authentication) {
                return user;
            }
        };

        auth = new UsernamePasswordAuthenticationToken("petugas@test.com", "pass");
    }

    @Test
    void daftarSemuaLaporanPetugas() {
        Laporan lap = new Laporan();
        lap.setId(10);
        lap.setJudul("Laporan Banjir");
        lap.setStatus(Laporan.STATUS_DRAFT);

        LaporanRepository laporanRepo = (LaporanRepository) Proxy.newProxyInstance(
                LaporanRepository.class.getClassLoader(),
                new Class<?>[] { LaporanRepository.class },
                (proxy, method, args) -> {
                    if ("findByUserIdOrderByUpdatedAtDesc".equals(method.getName())) {
                        return List.of(lap);
                    }
                    return null;
                });

        DokumentasiRepository dokRepo = (DokumentasiRepository) Proxy.newProxyInstance(
                DokumentasiRepository.class.getClassLoader(),
                new Class<?>[] { DokumentasiRepository.class },
                (proxy, method, args) -> {
                    if ("countByLaporanId".equals(method.getName())) {
                        return 2L;
                    }
                    return null;
                });

        LaporanPetugasController controller = new LaporanPetugasController(
                currentUserService, laporanRepo, null, dokRepo, null, null);

        Model model = new ConcurrentModel();
        String view = controller.daftar(auth, null, model);

        assertEquals("petugas/laporan/daftar", view);
        assertEquals(List.of(lap), model.getAttribute("laporanList"));
        Map<?, ?> jumlahDok = (Map<?, ?>) model.getAttribute("jumlahDokumentasi");
        assertEquals(2L, jumlahDok.get(10));
    }

    @Test
    void daftarLaporanPetugasDenganFilterStatus() {
        Laporan lap = new Laporan();
        lap.setId(11);
        lap.setStatus(Laporan.STATUS_TERKIRIM);

        LaporanRepository laporanRepo = (LaporanRepository) Proxy.newProxyInstance(
                LaporanRepository.class.getClassLoader(),
                new Class<?>[] { LaporanRepository.class },
                (proxy, method, args) -> {
                    if ("findByUserIdAndStatusOrderByUpdatedAtDesc".equals(method.getName())) {
                        assertEquals("diajukan", args[1]);
                        return List.of(lap);
                    }
                    return null;
                });

        DokumentasiRepository dokRepo = (DokumentasiRepository) Proxy.newProxyInstance(
                DokumentasiRepository.class.getClassLoader(),
                new Class<?>[] { DokumentasiRepository.class },
                (proxy, method, args) -> 0L);

        LaporanPetugasController controller = new LaporanPetugasController(
                currentUserService, laporanRepo, null, dokRepo, null, null);

        Model model = new ConcurrentModel();
        String view = controller.daftar(auth, "diajukan", model);

        assertEquals("petugas/laporan/daftar", view);
        assertEquals(List.of(lap), model.getAttribute("laporanList"));
    }

    @Test
    void buatFormMenampilkanDaftarBencana() {
        Bencana bencana = new Bencana();
        bencana.setId(5);
        bencana.setNama("Banjir Lahar");

        BencanaRepository bencanaRepo = (BencanaRepository) Proxy.newProxyInstance(
                BencanaRepository.class.getClassLoader(),
                new Class<?>[] { BencanaRepository.class },
                (proxy, method, args) -> {
                    if ("findAllByOrderByNamaAsc".equals(method.getName())) {
                        return List.of(bencana);
                    }
                    return null;
                });

        LaporanPetugasController controller = new LaporanPetugasController(
                currentUserService, null, bencanaRepo, null, null, null);

        Model model = new ConcurrentModel();
        String view = controller.buatForm(model);

        assertEquals("petugas/laporan/buat", view);
        assertEquals(List.of(bencana), model.getAttribute("bencanaList"));
    }

    @Test
    void simpanLaporanDraftSukses() {
        Bencana bencana = new Bencana();
        bencana.setId(1);

        BencanaRepository bencanaRepo = (BencanaRepository) Proxy.newProxyInstance(
                BencanaRepository.class.getClassLoader(),
                new Class<?>[] { BencanaRepository.class },
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) {
                        return Optional.of(bencana);
                    }
                    return Optional.empty();
                });

        List<Laporan> saved = new ArrayList<>();
        LaporanRepository laporanRepo = (LaporanRepository) Proxy.newProxyInstance(
                LaporanRepository.class.getClassLoader(),
                new Class<?>[] { LaporanRepository.class },
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) {
                        Laporan entity = (Laporan) args[0];
                        entity.setId(101);
                        saved.add(entity);
                        return entity;
                    }
                    return null;
                });

        LaporanPetugasController controller = new LaporanPetugasController(
                currentUserService, laporanRepo, bencanaRepo, null, null, null);

        RedirectAttributes ra = new RedirectAttributesModelMap();
        String redirect = controller.simpan(auth, 1, "Judul Draft", "Isi Laporan", "draft", ra);

        assertEquals("redirect:/petugas/laporan/101", redirect);
        assertEquals(1, saved.size());
        assertEquals(Laporan.STATUS_DRAFT, saved.get(0).getStatus());
        assertEquals("Judul Draft", saved.get(0).getJudul());
        assertEquals(user, saved.get(0).getUser());
        assertTrue(ra.getFlashAttributes().containsKey("sukses"));
    }

    @Test
    void simpanLaporanAjukanLangsungSukses() {
        Bencana bencana = new Bencana();
        bencana.setId(1);

        BencanaRepository bencanaRepo = (BencanaRepository) Proxy.newProxyInstance(
                BencanaRepository.class.getClassLoader(),
                new Class<?>[] { BencanaRepository.class },
                (proxy, method, args) -> Optional.of(bencana));

        List<Laporan> saved = new ArrayList<>();
        LaporanRepository laporanRepo = (LaporanRepository) Proxy.newProxyInstance(
                LaporanRepository.class.getClassLoader(),
                new Class<?>[] { LaporanRepository.class },
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) {
                        Laporan entity = (Laporan) args[0];
                        entity.setId(102);
                        saved.add(entity);
                        return entity;
                    }
                    return null;
                });

        LaporanPetugasController controller = new LaporanPetugasController(
                currentUserService, laporanRepo, bencanaRepo, null, null, null);

        RedirectAttributes ra = new RedirectAttributesModelMap();
        String redirect = controller.simpan(auth, 1, "Judul Ajuan", "Isi Laporan Ajuan", "ajukan", ra);

        assertEquals("redirect:/petugas/laporan/102", redirect);
        assertEquals(1, saved.size());
        assertEquals(Laporan.STATUS_TERKIRIM, saved.get(0).getStatus());
    }

    @Test
    void simpanLaporanValidasiGagal() {
        LaporanPetugasController controller = new LaporanPetugasController(
                currentUserService, null, null, null, null, null);

        RedirectAttributes ra = new RedirectAttributesModelMap();
        String redirect = controller.simpan(auth, null, "", "", "draft", ra);

        assertEquals("redirect:/petugas/laporan/buat", redirect);
        assertTrue(ra.getFlashAttributes().containsKey("error"));
    }

    @Test
    void detailLaporanMilikPetugas() {
        Laporan lap = new Laporan();
        lap.setId(10);
        lap.setJudul("Laporan Banjir");

        LaporanRepository laporanRepo = (LaporanRepository) Proxy.newProxyInstance(
                LaporanRepository.class.getClassLoader(),
                new Class<?>[] { LaporanRepository.class },
                (proxy, method, args) -> {
                    if ("findByIdAndUserId".equals(method.getName())) {
                        return Optional.of(lap);
                    }
                    return Optional.empty();
                });

        Dokumentasi dok = new Dokumentasi();
        dok.setId(1);
        DokumentasiRepository dokRepo = (DokumentasiRepository) Proxy.newProxyInstance(
                DokumentasiRepository.class.getClassLoader(),
                new Class<?>[] { DokumentasiRepository.class },
                (proxy, method, args) -> List.of(dok));

        Feedback fb = new Feedback();
        fb.setId(1);
        FeedbackRepository feedbackRepo = (FeedbackRepository) Proxy.newProxyInstance(
                FeedbackRepository.class.getClassLoader(),
                new Class<?>[] { FeedbackRepository.class },
                (proxy, method, args) -> List.of(fb));

        LaporanPetugasController controller = new LaporanPetugasController(
                currentUserService, laporanRepo, null, dokRepo, feedbackRepo, null);

        Model model = new ConcurrentModel();
        RedirectAttributes ra = new RedirectAttributesModelMap();
        String view = controller.detail(auth, 10, model, ra);

        assertEquals("petugas/laporan/detail", view);
        assertEquals(lap, model.getAttribute("laporan"));
        assertEquals(List.of(dok), model.getAttribute("berkasList"));
        assertEquals(List.of(fb), model.getAttribute("feedbackList"));
    }

    @Test
    void detailLaporanBukanMilikPetugas() {
        LaporanRepository laporanRepo = (LaporanRepository) Proxy.newProxyInstance(
                LaporanRepository.class.getClassLoader(),
                new Class<?>[] { LaporanRepository.class },
                (proxy, method, args) -> Optional.empty());

        LaporanPetugasController controller = new LaporanPetugasController(
                currentUserService, laporanRepo, null, null, null, null);

        Model model = new ConcurrentModel();
        RedirectAttributes ra = new RedirectAttributesModelMap();
        String view = controller.detail(auth, 999, model, ra);

        assertEquals("redirect:/petugas/laporan", view);
        assertTrue(ra.getFlashAttributes().containsKey("error"));
    }

    @Test
    void editFormLaporanDraftSukses() {
        Laporan lap = new Laporan();
        lap.setId(10);
        lap.setStatus(Laporan.STATUS_DRAFT);

        LaporanRepository laporanRepo = (LaporanRepository) Proxy.newProxyInstance(
                LaporanRepository.class.getClassLoader(),
                new Class<?>[] { LaporanRepository.class },
                (proxy, method, args) -> Optional.of(lap));

        Bencana bencana = new Bencana();
        BencanaRepository bencanaRepo = (BencanaRepository) Proxy.newProxyInstance(
                BencanaRepository.class.getClassLoader(),
                new Class<?>[] { BencanaRepository.class },
                (proxy, method, args) -> List.of(bencana));

        LaporanPetugasController controller = new LaporanPetugasController(
                currentUserService, laporanRepo, bencanaRepo, null, null, null);

        Model model = new ConcurrentModel();
        RedirectAttributes ra = new RedirectAttributesModelMap();
        String view = controller.editForm(auth, 10, model, ra);

        assertEquals("petugas/laporan/edit", view);
        assertEquals(lap, model.getAttribute("laporan"));
    }

    @Test
    void editFormLaporanNonDraftDitolak() {
        Laporan lap = new Laporan();
        lap.setId(10);
        lap.setStatus(Laporan.STATUS_TERKIRIM);

        LaporanRepository laporanRepo = (LaporanRepository) Proxy.newProxyInstance(
                LaporanRepository.class.getClassLoader(),
                new Class<?>[] { LaporanRepository.class },
                (proxy, method, args) -> Optional.of(lap));

        LaporanPetugasController controller = new LaporanPetugasController(
                currentUserService, laporanRepo, null, null, null, null);

        Model model = new ConcurrentModel();
        RedirectAttributes ra = new RedirectAttributesModelMap();
        String view = controller.editForm(auth, 10, model, ra);

        assertEquals("redirect:/petugas/laporan/10", view);
        assertTrue(ra.getFlashAttributes().containsKey("error"));
    }

    @Test
    void ajukanLaporanDraftSukses() {
        Laporan lap = new Laporan();
        lap.setId(10);
        lap.setStatus(Laporan.STATUS_DRAFT);

        LaporanRepository laporanRepo = (LaporanRepository) Proxy.newProxyInstance(
                LaporanRepository.class.getClassLoader(),
                new Class<?>[] { LaporanRepository.class },
                (proxy, method, args) -> {
                    if ("findByIdAndUserId".equals(method.getName())) {
                        return Optional.of(lap);
                    }
                    if ("save".equals(method.getName())) {
                        return args[0];
                    }
                    return null;
                });

        LaporanPetugasController controller = new LaporanPetugasController(
                currentUserService, laporanRepo, null, null, null, null);

        RedirectAttributes ra = new RedirectAttributesModelMap();
        String redirect = controller.ajukan(auth, 10, ra);

        assertEquals("redirect:/petugas/laporan/10", redirect);
        assertEquals(Laporan.STATUS_TERKIRIM, lap.getStatus());
        assertTrue(ra.getFlashAttributes().containsKey("sukses"));
    }

    @Test
    void hapusLaporanDraftSukses() {
        Laporan lap = new Laporan();
        lap.setId(10);
        lap.setStatus(Laporan.STATUS_DRAFT);

        boolean[] deleted = {false};
        LaporanRepository laporanRepo = (LaporanRepository) Proxy.newProxyInstance(
                LaporanRepository.class.getClassLoader(),
                new Class<?>[] { LaporanRepository.class },
                (proxy, method, args) -> {
                    if ("findByIdAndUserId".equals(method.getName())) {
                        return Optional.of(lap);
                    }
                    if ("delete".equals(method.getName())) {
                        deleted[0] = true;
                        return null;
                    }
                    return null;
                });

        DokumentasiStorageService storageService = new DokumentasiStorageService(null, null, "uploads") {
            @Override
            public void hapusSemuaDokumentasiLaporan(User u, Integer lapId) {
                // mock no-op
            }
        };

        LaporanPetugasController controller = new LaporanPetugasController(
                currentUserService, laporanRepo, null, null, null, storageService);

        RedirectAttributes ra = new RedirectAttributesModelMap();
        String redirect = controller.hapus(auth, 10, ra);

        assertEquals("redirect:/petugas/laporan", redirect);
        assertTrue(deleted[0]);
        assertTrue(ra.getFlashAttributes().containsKey("sukses"));
    }

    @Test
    void hapusLaporanNonDraftDitolak() {
        Laporan lap = new Laporan();
        lap.setId(10);
        lap.setStatus(Laporan.STATUS_TERKIRIM);

        LaporanRepository laporanRepo = (LaporanRepository) Proxy.newProxyInstance(
                LaporanRepository.class.getClassLoader(),
                new Class<?>[] { LaporanRepository.class },
                (proxy, method, args) -> Optional.of(lap));

        LaporanPetugasController controller = new LaporanPetugasController(
                currentUserService, laporanRepo, null, null, null, null);

        RedirectAttributes ra = new RedirectAttributesModelMap();
        String redirect = controller.hapus(auth, 10, ra);

        assertEquals("redirect:/petugas/laporan/10", redirect);
        assertTrue(ra.getFlashAttributes().containsKey("error"));
    }
}

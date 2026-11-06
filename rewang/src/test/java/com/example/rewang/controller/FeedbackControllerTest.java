package com.example.rewang.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.rewang.entity.Feedback;
import com.example.rewang.entity.Laporan;
import com.example.rewang.entity.User;
import com.example.rewang.repository.FeedbackRepository;
import com.example.rewang.repository.LaporanRepository;
import com.example.rewang.service.CurrentUserService;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

class FeedbackControllerTest {

    @Test
    void daftarSemuaFeedbackPetugas() {
        User user = new User();
        user.setId(1);

        CurrentUserService currentUserService = new CurrentUserService(null) {
            @Override
            public User requireUser(Authentication authentication) {
                return user;
            }
        };

        Feedback expectedFb = new Feedback();
        expectedFb.setId(10);
        expectedFb.setIsi("Perbaiki dokumentasi");

        FeedbackRepository feedbackRepo = (FeedbackRepository) Proxy.newProxyInstance(
                FeedbackRepository.class.getClassLoader(),
                new Class<?>[] { FeedbackRepository.class },
                (proxy, method, args) -> {
                    if ("findByLaporanUserIdOrderByCreatedAtDesc".equals(method.getName())) {
                        return List.of(expectedFb);
                    }
                    return null;
                });

        LaporanRepository laporanRepo = (LaporanRepository) Proxy.newProxyInstance(
                LaporanRepository.class.getClassLoader(),
                new Class<?>[] { LaporanRepository.class },
                (proxy, method, args) -> null);

        FeedbackController controller = new FeedbackController(currentUserService, feedbackRepo, laporanRepo);

        Authentication auth = new UsernamePasswordAuthenticationToken("petugas@test.com", "pass");
        Model model = new ConcurrentModel();
        String view = controller.daftar(auth, null, model);

        assertEquals("petugas/feedback/daftar", view);
        assertEquals(List.of(expectedFb), model.getAttribute("feedbackList"));
    }

    @Test
    void daftarFeedbackDenganFilterLaporanId() {
        User user = new User();
        user.setId(1);

        CurrentUserService currentUserService = new CurrentUserService(null) {
            @Override
            public User requireUser(Authentication authentication) {
                return user;
            }
        };

        Laporan laporan = new Laporan();
        laporan.setId(99);

        Feedback expectedFb = new Feedback();
        expectedFb.setId(20);

        FeedbackRepository feedbackRepo = (FeedbackRepository) Proxy.newProxyInstance(
                FeedbackRepository.class.getClassLoader(),
                new Class<?>[] { FeedbackRepository.class },
                (proxy, method, args) -> {
                    if ("findByLaporanIdOrderByCreatedAtDesc".equals(method.getName())) {
                        return List.of(expectedFb);
                    }
                    return null;
                });

        LaporanRepository laporanRepo = (LaporanRepository) Proxy.newProxyInstance(
                LaporanRepository.class.getClassLoader(),
                new Class<?>[] { LaporanRepository.class },
                (proxy, method, args) -> {
                    if ("findByIdAndUserId".equals(method.getName())) {
                        return Optional.of(laporan);
                    }
                    return Optional.empty();
                });

        FeedbackController controller = new FeedbackController(currentUserService, feedbackRepo, laporanRepo);

        Authentication auth = new UsernamePasswordAuthenticationToken("petugas@test.com", "pass");
        Model model = new ConcurrentModel();
        String view = controller.daftar(auth, 99, model);

        assertEquals("petugas/feedback/daftar", view);
        assertEquals(laporan, model.getAttribute("selectedLaporan"));
        assertEquals(List.of(expectedFb), model.getAttribute("feedbackList"));
    }
}

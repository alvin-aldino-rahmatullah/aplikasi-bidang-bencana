package com.example.rewang.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class DokumentasiStorageServiceTest {

    @Test
    void menolakFileKosong() {
        MockMultipartFile file = new MockMultipartFile("file", "foto.jpg", "image/jpeg", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> DokumentasiStorageService.validasiFile(file));
    }

    @Test
    void menolakEkstensiLain() {
        MockMultipartFile file = new MockMultipartFile("file", "script.exe", "application/octet-stream", new byte[] {1, 2, 3});
        assertThrows(IllegalArgumentException.class, () -> DokumentasiStorageService.validasiFile(file));
    }

    @Test
    void menerimaJpg() {
        MockMultipartFile file = new MockMultipartFile("file", "foto.jpg", "image/jpeg", new byte[] {1, 2, 3});
        assertDoesNotThrow(() -> DokumentasiStorageService.validasiFile(file));
    }
}

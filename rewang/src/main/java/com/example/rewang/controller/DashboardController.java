package com.example.rewang.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/petugas/dashboard")
    public String petugasDashboard() {
        return "petugas/dashboard";
    }
}

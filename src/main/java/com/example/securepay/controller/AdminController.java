package com.example.securepay.controller;

import com.example.securepay.dto.AdminMerchantResponse;
import com.example.securepay.dto.AdminPaymentResponse;
import com.example.securepay.dto.AdminWebhookResponse;
import com.example.securepay.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/merchants")
    public ResponseEntity<List<AdminMerchantResponse>>
    getAllMerchants() {
        return ResponseEntity.ok(
                adminService.getAllMerchants()
        );
    }

    @GetMapping("/payments")
    public ResponseEntity<List<AdminPaymentResponse>>
    getAllPayments() {
        return ResponseEntity.ok(
                adminService.getAllPayments()
        );
    }

    @GetMapping("/webhooks/failed")
    public ResponseEntity<List<AdminWebhookResponse>>
    getFailedWebhooks() {
        return ResponseEntity.ok(
                adminService.getFailedWebhooks()
        );
    }
}
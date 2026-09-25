package com.example.securepay.controller;

import com.example.securepay.dto.CreateCustomerRequest;
import com.example.securepay.dto.CustomerResponse;
import com.example.securepay.entity.Merchant;
import com.example.securepay.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @AuthenticationPrincipal Merchant merchant,
            @Valid @RequestBody CreateCustomerRequest request
    ) {
        CustomerResponse response =
                customerService.createCustomer(
                        merchant.getId(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomer(
            @AuthenticationPrincipal Merchant merchant,
            @PathVariable Long customerId
    ) {
        CustomerResponse response =
                customerService.getCustomer(
                        merchant.getId(),
                        customerId
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers(
            @AuthenticationPrincipal Merchant merchant
    ) {
        List<CustomerResponse> response =
                customerService.getAllCustomers(merchant.getId());

        return ResponseEntity.ok(response);
    }
}
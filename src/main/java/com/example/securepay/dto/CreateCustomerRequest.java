package com.example.securepay.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(

        @NotBlank(message = "Full name is required")
        @Size(max = 150, message = "Full name cannot exceed 150 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @Pattern(
                regexp = "^\\+?[0-9]{10,15}$",
                message = "Phone number must contain 10 to 15 digits"
        )
        String phoneNumber
) {
}
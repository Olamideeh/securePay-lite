package com.example.securepay.service;

import com.example.securepay.dto.CreateCustomerRequest;
import com.example.securepay.dto.CustomerResponse;
import com.example.securepay.entity.Customer;
import com.example.securepay.entity.Merchant;
import com.example.securepay.exception.DuplicateResourceException;
import com.example.securepay.exception.ResourceNotFoundException;
import com.example.securepay.repository.CustomerRepository;
import com.example.securepay.repository.MerchantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final MerchantRepository merchantRepository;

    public CustomerService(
            CustomerRepository customerRepository,
            MerchantRepository merchantRepository
    ) {
        this.customerRepository = customerRepository;
        this.merchantRepository = merchantRepository;
    }

    @Transactional
    public CustomerResponse createCustomer(
            Long merchantId,
            CreateCustomerRequest request
    ) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Merchant not found with ID: " + merchantId
                ));

        String normalizedEmail = request.email()
                .trim()
                .toLowerCase();

        boolean emailAlreadyExists =
                customerRepository.existsByMerchant_IdAndEmailIgnoreCase(
                        merchantId,
                        normalizedEmail
                );

        if (emailAlreadyExists) {
            throw new DuplicateResourceException(
                    "A customer with this email already exists for this merchant"
            );
        }

        Customer customer = Customer.builder()
                .fullName(request.fullName().trim())
                .email(normalizedEmail)
                .phoneNumber(normalizePhoneNumber(request.phoneNumber()))
                .merchant(merchant)
                .build();

        Customer savedCustomer = customerRepository.save(customer);

        return mapToResponse(savedCustomer);
    }
    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(
            Long merchantId,
            Long customerId
    ) {
        Customer customer = customerRepository
                .findByIdAndMerchant_Id(customerId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with ID: " + customerId
                ));

        return mapToResponse(customer);
    }

    @Transactional(readOnly = true)
    public java.util.List<CustomerResponse> getAllCustomers(
            Long merchantId
    ) {
        return customerRepository.findAllByMerchant_Id(merchantId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getMerchant().getId(),
                customer.getCreatedAt()
        );
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }

        return phoneNumber.trim();
    }
}
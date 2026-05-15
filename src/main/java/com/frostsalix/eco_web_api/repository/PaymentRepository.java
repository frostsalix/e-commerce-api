package com.frostsalix.eco_web_api.repository;

import com.frostsalix.eco_web_api.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
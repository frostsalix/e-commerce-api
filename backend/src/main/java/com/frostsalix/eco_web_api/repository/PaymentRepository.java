package com.frostsalix.eco_web_api.repository;

import com.frostsalix.eco_web_api.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOutTradeNo(String outTradeNo);
    Optional<Payment> findByOrderId(Long orderId);
}
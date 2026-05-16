package com.frostsalix.eco_web_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    private Double amount;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    private String outTradeNo;
    private String gatewayTradeNo;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}
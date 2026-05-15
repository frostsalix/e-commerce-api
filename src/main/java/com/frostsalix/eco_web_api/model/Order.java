package com.frostsalix.eco_web_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private Double totalPrice;

    @Setter
    private LocalDateTime createdAt;

    @Setter
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Order() {
    }

}
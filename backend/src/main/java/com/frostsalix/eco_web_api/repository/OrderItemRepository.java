package com.frostsalix.eco_web_api.repository;

import com.frostsalix.eco_web_api.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {
}
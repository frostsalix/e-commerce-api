package com.frostsalix.eco_web_api.repository;

import com.frostsalix.eco_web_api.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository
        extends JpaRepository<CartItem, Long> {
}
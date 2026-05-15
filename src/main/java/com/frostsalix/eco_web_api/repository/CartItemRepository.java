package com.frostsalix.eco_web_api.repository;

import com.frostsalix.eco_web_api.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import com.frostsalix.eco_web_api.model.User;

import java.util.List;

public interface CartItemRepository
        extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
}
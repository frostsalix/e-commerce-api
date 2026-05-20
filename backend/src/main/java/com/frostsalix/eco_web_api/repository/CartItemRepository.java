package com.frostsalix.eco_web_api.repository;

import com.frostsalix.eco_web_api.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import com.frostsalix.eco_web_api.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CartItemRepository
        extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);

    Page<CartItem> findByUser(User user, Pageable pageable);
}
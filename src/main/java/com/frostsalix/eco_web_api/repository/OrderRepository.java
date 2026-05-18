package com.frostsalix.eco_web_api.repository;

import com.frostsalix.eco_web_api.model.Order;
import com.frostsalix.eco_web_api.model.OrderStatus;
import com.frostsalix.eco_web_api.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository
        extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    Page<Order> findByUser(User user, Pageable pageable);

    long countByStatus(OrderStatus status);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}
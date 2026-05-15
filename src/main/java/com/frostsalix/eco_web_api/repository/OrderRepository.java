package com.frostsalix.eco_web_api.repository;

import com.frostsalix.eco_web_api.model.Order;
import com.frostsalix.eco_web_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository
        extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);
}
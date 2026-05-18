package com.frostsalix.eco_web_api.repository;

import com.frostsalix.eco_web_api.model.Order;
import com.frostsalix.eco_web_api.model.OrderStatus;
import com.frostsalix.eco_web_api.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository
        extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    Page<Order> findByUser(User user, Pageable pageable);

    long countByStatus(OrderStatus status);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.status IN :statuses")
    Double sumTotalByStatusIn(@Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.status IN :statuses AND o.createdAt >= :since")
    Double sumTotalByStatusInSince(@Param("statuses") List<OrderStatus> statuses, @Param("since") LocalDateTime since);
}
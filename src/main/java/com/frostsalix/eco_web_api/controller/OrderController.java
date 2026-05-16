package com.frostsalix.eco_web_api.controller;

import com.frostsalix.eco_web_api.common.ApiResponse;
import com.frostsalix.eco_web_api.model.Order;
import com.frostsalix.eco_web_api.model.OrderStatus;
import com.frostsalix.eco_web_api.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 创建订单
    @PostMapping
    public ApiResponse<Order> createOrder() {

        return ApiResponse.success(
                orderService.createOrder()
        );
    }

    // 查看我的订单
    @GetMapping
    public ApiResponse<Page<Order>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return ApiResponse.success(
                orderService.getMyOrders(page, size)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<Order> getOrder(@PathVariable Long id) {
        return ApiResponse.success(
                orderService.getOrder(id)
        );
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<Order> cancelOrder(@PathVariable Long id) {
        return ApiResponse.success(
                orderService.cancelOrder(id)
        );
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Order> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status
    ) {
        return ApiResponse.success(
                orderService.updateStatus(id, status)
        );
    }
}
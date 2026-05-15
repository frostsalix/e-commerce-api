package com.frostsalix.eco_web_api.controller;

import com.frostsalix.eco_web_api.common.ApiResponse;
import com.frostsalix.eco_web_api.model.Order;
import com.frostsalix.eco_web_api.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public ApiResponse<List<Order>> getMyOrders() {

        return ApiResponse.success(
                orderService.getMyOrders()
        );
    }
}
package com.frostsalix.eco_web_api.controller;

import com.frostsalix.eco_web_api.common.ApiResponse;
import com.frostsalix.eco_web_api.model.Order;
import com.frostsalix.eco_web_api.model.OrderStatus;
import com.frostsalix.eco_web_api.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@Tag(name = "订单", description = "订单创建、查看、取消、发货、送达")
@Validated
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "创建订单")
    public ApiResponse<Order> createOrder() {

        return ApiResponse.success(
                orderService.createOrder()
        );
    }

    @GetMapping
    @Operation(summary = "查看我的订单")
    public ApiResponse<Page<Order>> getMyOrders(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {

        return ApiResponse.success(
                orderService.getMyOrders(page, size)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "查看订单详情")
    public ApiResponse<Order> getOrder(@PathVariable Long id) {
        return ApiResponse.success(
                orderService.getOrder(id)
        );
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单")
    public ApiResponse<Order> cancelOrder(@PathVariable Long id) {
        return ApiResponse.success(
                orderService.cancelOrder(id)
        );
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "修改订单状态（管理员）")
    public ApiResponse<Order> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status
    ) {
        return ApiResponse.success(
                orderService.updateStatus(id, status)
        );
    }

    @PutMapping("/{id}/ship")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "发货（管理员）")
    public ApiResponse<Order> shipOrder(
            @PathVariable Long id,
            @RequestParam @NotBlank String trackingNumber
    ) {
        return ApiResponse.success(
                orderService.shipOrder(id, trackingNumber)
        );
    }

    @PutMapping("/{id}/deliver")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "确认送达（管理员）")
    public ApiResponse<Order> deliverOrder(@PathVariable Long id) {
        return ApiResponse.success(
                orderService.deliverOrder(id)
        );
    }
}
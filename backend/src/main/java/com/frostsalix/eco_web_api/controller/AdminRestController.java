package com.frostsalix.eco_web_api.controller;

import com.frostsalix.eco_web_api.common.ApiResponse;
import com.frostsalix.eco_web_api.model.Order;
import com.frostsalix.eco_web_api.model.OrderStatus;
import com.frostsalix.eco_web_api.model.Product;
import com.frostsalix.eco_web_api.model.User;
import com.frostsalix.eco_web_api.repository.OrderRepository;
import com.frostsalix.eco_web_api.repository.ProductRepository;
import com.frostsalix.eco_web_api.repository.UserRepository;
import com.frostsalix.eco_web_api.service.OrderService;
import com.frostsalix.eco_web_api.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRestController {

    private static final List<OrderStatus> REVENUE_STATUSES = List.of(
            OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.DONE
    );

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;
    private final UserService userService;

    public AdminRestController(
            ProductRepository productRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            OrderService orderService,
            UserService userService
    ) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("productCount", productRepository.count());
        data.put("orderCount", orderRepository.count());
        data.put("userCount", userRepository.count());
        data.put("pendingOrders", orderRepository.countByStatus(OrderStatus.PENDING));
        data.put("paidOrders", orderRepository.countByStatus(OrderStatus.PAID));
        data.put("shippedOrders", orderRepository.countByStatus(OrderStatus.SHIPPED));
        data.put("doneOrders", orderRepository.countByStatus(OrderStatus.DONE));
        data.put("cancelledOrders", orderRepository.countByStatus(OrderStatus.CANCELLED));
        data.put("totalRevenue", orderRepository.sumTotalByStatusIn(REVENUE_STATUSES));
        data.put("todayRevenue", orderRepository.sumTotalByStatusInSince(REVENUE_STATUSES, LocalDate.now().atStartOfDay()));
        data.put("recentOrders", orderRepository.findAll(
                PageRequest.of(0, 10, Sort.by("createdAt").descending())
        ).getContent());
        data.put("lowStockProducts", productRepository.findByStockLessThan(5));
        return ApiResponse.success(data);
    }

    @GetMapping("/orders")
    public ApiResponse<Page<Order>> orders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status
    ) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var orderPage = (status != null && !status.isBlank())
                ? orderRepository.findByStatus(OrderStatus.valueOf(status), pageable)
                : orderRepository.findAll(pageable);
        return ApiResponse.success(orderPage);
    }

    @PutMapping("/orders/{id}/cancel")
    public ApiResponse<Order> cancelOrder(@PathVariable Long id) {
        return ApiResponse.success(orderService.cancelOrderByAdmin(id));
    }

    @GetMapping("/users")
    public ApiResponse<Page<User>> users(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(userService.getAllUsers(PageRequest.of(page, size)));
    }

    @PutMapping("/users/{id}/role")
    public ApiResponse<String> updateRole(
            @PathVariable Long id,
            @RequestParam String role
    ) {
        userService.updateRole(id, role);
        return ApiResponse.success("User role updated to " + role);
    }
}

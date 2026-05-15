package com.frostsalix.eco_web_api.service;

import com.frostsalix.eco_web_api.model.*;
import com.frostsalix.eco_web_api.repository.CartItemRepository;
import com.frostsalix.eco_web_api.repository.OrderRepository;
import com.frostsalix.eco_web_api.repository.ProductRepository;
import com.frostsalix.eco_web_api.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderService(
            OrderRepository orderRepository,
            CartItemRepository cartItemRepository,
            UserRepository userRepository,
            ProductRepository productRepository
    ) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Order createOrder() {

        String username = Objects.requireNonNull(SecurityContextHolder.getContext()
                        .getAuthentication())
                .getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<CartItem> cartItems = cartItemRepository.findByUser(user);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("购物车为空");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        double total = 0;

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cart : cartItems) {

            Product product = productRepository.findById(cart.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("商品不存在"));

            if (product.getStock() < cart.getQuantity()) {
                throw new RuntimeException("库存不足：" + product.getName());
            }

            OrderItem item = new OrderItem();
            item.setProductName(product.getName());
            item.setProductPrice(product.getPrice());
            item.setQuantity(cart.getQuantity());
            item.setOrder(order);

            orderItems.add(item);

            total += product.getPrice() * cart.getQuantity();
        }

        order.setItems(orderItems);
        order.setTotalPrice(total);

        Order saved = orderRepository.save(order);

        cartItemRepository.deleteAll(cartItems);

        return saved;
    }

    public List<Order> getMyOrders() {

        String username = Objects.requireNonNull(SecurityContextHolder
                        .getContext()
                        .getAuthentication())
                .getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow();

        return orderRepository.findByUser(user);
    }

    public Order updateStatus(Long orderId, OrderStatus targetStatus) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        OrderStatus current = order.getStatus();

        boolean allowed = switch (current) {
            case PENDING -> targetStatus == OrderStatus.PAID
                    || targetStatus == OrderStatus.CANCELLED;
            case PAID -> targetStatus == OrderStatus.SHIPPED
                    || targetStatus == OrderStatus.CANCELLED;
            case SHIPPED -> targetStatus == OrderStatus.DONE;
            case CANCELLED, DONE -> false;
        };

        if (!allowed) {
            throw new RuntimeException(
                    "不允许从 " + current + " 转换到 " + targetStatus);
        }

        order.setStatus(targetStatus);

        return orderRepository.save(order);
    }

}
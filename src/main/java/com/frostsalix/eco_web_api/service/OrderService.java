package com.frostsalix.eco_web_api.service;

import com.frostsalix.eco_web_api.model.*;
import com.frostsalix.eco_web_api.repository.CartItemRepository;
import com.frostsalix.eco_web_api.repository.OrderRepository;
import com.frostsalix.eco_web_api.repository.PaymentRepository;
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
    private final PaymentRepository paymentRepository;

    public OrderService(
            OrderRepository orderRepository,
            CartItemRepository cartItemRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            PaymentRepository paymentRepository
    ) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.paymentRepository = paymentRepository;
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

    public Order getOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        String username = Objects.requireNonNull(SecurityContextHolder
                        .getContext()
                        .getAuthentication())
                .getName();

        if (!order.getUser().getUsername().equals(username)) {
            throw new RuntimeException("无权查看该订单");
        }

        return order;
    }

    @Transactional
    public Order cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        String username = Objects.requireNonNull(SecurityContextHolder
                        .getContext()
                        .getAuthentication())
                .getName();

        if (!order.getUser().getUsername().equals(username)) {
            throw new RuntimeException("无权取消该订单");
        }

        OrderStatus current = order.getStatus();

        if (current == OrderStatus.CANCELLED) {
            throw new RuntimeException("订单已取消，不可重复操作");
        }

        if (current == OrderStatus.DONE) {
            throw new RuntimeException("已完成订单不可取消");
        }

        if (current == OrderStatus.SHIPPED) {
            throw new RuntimeException("已发货订单不可取消");
        }

        // 已支付订单：回滚库存
        if (current == OrderStatus.PAID) {
            for (OrderItem item : order.getItems()) {
                Product product = productRepository.findByIdForUpdate(
                        item.getProduct().getId()
                );
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        return order;
    }

}
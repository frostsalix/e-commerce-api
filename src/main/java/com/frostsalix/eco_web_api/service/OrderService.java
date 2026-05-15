package com.frostsalix.eco_web_api.service;

import com.frostsalix.eco_web_api.model.CartItem;
import com.frostsalix.eco_web_api.model.Order;
import com.frostsalix.eco_web_api.model.Product;
import com.frostsalix.eco_web_api.model.User;
import com.frostsalix.eco_web_api.repository.CartItemRepository;
import com.frostsalix.eco_web_api.repository.OrderRepository;
import com.frostsalix.eco_web_api.repository.ProductRepository;
import com.frostsalix.eco_web_api.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public Order createOrder() {

        // 当前用户
        String username = Objects.requireNonNull(SecurityContextHolder
                        .getContext()
                        .getAuthentication())
                .getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow();

        // 获取购物车
        List<CartItem> cartItems =
                cartItemRepository.findByUser(user);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("购物车为空");
        }

        // 计算总价
        double totalPrice = 0;

        for (CartItem item : cartItems) {

            Product product = item.getProduct();
            // 库存检查
            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException(
                        product.getName() + " 库存不足"
                );
            }

            // 扣库存
            product.setStock(
                    product.getStock() - item.getQuantity()
            );

            productRepository.save(product);
            totalPrice +=
                    product.getPrice() * item.getQuantity();
        }

        // 创建订单
        Order order = new Order();

        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalPrice(totalPrice);
        order.setStatus("PENDING");

        Order savedOrder = orderRepository.save(order);

        // 清空购物车
        cartItemRepository.deleteAll(cartItems);

        return savedOrder;
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
}
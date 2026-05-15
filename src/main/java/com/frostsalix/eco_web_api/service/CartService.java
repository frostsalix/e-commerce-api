package com.frostsalix.eco_web_api.service;

import com.frostsalix.eco_web_api.dto.AddToCartDTO;
import com.frostsalix.eco_web_api.model.CartItem;
import com.frostsalix.eco_web_api.model.Product;
import com.frostsalix.eco_web_api.model.User;
import com.frostsalix.eco_web_api.repository.CartItemRepository;
import com.frostsalix.eco_web_api.repository.ProductRepository;
import com.frostsalix.eco_web_api.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public CartItem addToCart(AddToCartDTO dto) {

        // 1. 获取当前登录用户名
        String username = Objects.requireNonNull(SecurityContextHolder
                        .getContext()
                        .getAuthentication())
                .getName();

        // 2. 查数据库用户
        User user = userRepository
                .findByUsername(username)
                .orElseThrow();

        // 3. 查商品
        Product product = productRepository
                .findById(dto.getProductId())
                .orElseThrow();

        // 4. 创建购物车项
        CartItem cartItem = new CartItem();

        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(dto.getQuantity());

        // 5. 保存
        return cartItemRepository.save(cartItem);
    }

    public List<CartItem> getMyCart() {

        String username = Objects.requireNonNull(SecurityContextHolder
                        .getContext()
                        .getAuthentication())
                .getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow();

        return cartItemRepository.findByUser(user);
    }

    public void removeCartItem(Long cartItemId) {

        String username = Objects.requireNonNull(SecurityContextHolder
                        .getContext()
                        .getAuthentication())
                .getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow();

        CartItem cartItem = cartItemRepository
                .findById(cartItemId)
                .orElseThrow();

        // 防止删除别人购物车
        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权限删除");
        }

        cartItemRepository.delete(cartItem);
    }
}
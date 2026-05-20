package com.frostsalix.eco_web_api.service;

import com.frostsalix.eco_web_api.dto.AddToCartDTO;
import com.frostsalix.eco_web_api.model.CartItem;
import com.frostsalix.eco_web_api.model.Product;
import com.frostsalix.eco_web_api.model.User;
import com.frostsalix.eco_web_api.repository.CartItemRepository;
import com.frostsalix.eco_web_api.repository.ProductRepository;
import com.frostsalix.eco_web_api.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    // 添加商品到当前用户购物车
    public CartItem addToCart(AddToCartDTO dto) {

        String username = Objects.requireNonNull(SecurityContextHolder
                        .getContext()
                        .getAuthentication())
                .getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow();

        Product product = productRepository
                .findById(dto.getProductId())
                .orElseThrow();

        CartItem cartItem = new CartItem();
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(dto.getQuantity());

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

    public Page<CartItem> getMyCart(int page, int size) {

        String username = Objects.requireNonNull(SecurityContextHolder
                        .getContext()
                        .getAuthentication())
                .getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow();

        return cartItemRepository.findByUser(user, PageRequest.of(page, size));
    }

    // 更新购物车商品数量，校验归属
    public CartItem updateQuantity(Long cartItemId, Integer quantity) {

        String username = Objects.requireNonNull(SecurityContextHolder
                        .getContext()
                        .getAuthentication())
                .getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow();

        CartItem cartItem = cartItemRepository
                .findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("购物车项不存在"));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权限修改");
        }

        if (quantity <= 0) {
            throw new RuntimeException("数量必须大于0");
        }

        cartItem.setQuantity(quantity);

        return cartItemRepository.save(cartItem);
    }

    // 删除购物车项，校验归属
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

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权限删除");
        }

        cartItemRepository.delete(cartItem);
    }
}
package com.frostsalix.eco_web_api.controller;

import com.frostsalix.eco_web_api.common.ApiResponse;
import com.frostsalix.eco_web_api.dto.AddToCartDTO;
import com.frostsalix.eco_web_api.model.CartItem;
import com.frostsalix.eco_web_api.service.CartService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // 添加购物车
    @PostMapping
    public ApiResponse<CartItem> addToCart(
            @RequestBody AddToCartDTO dto
    ) {

        return ApiResponse.success(
                cartService.addToCart(dto)
        );
    }

    // 查看我的购物车
    @GetMapping
    public ApiResponse<Page<CartItem>> getMyCart(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return ApiResponse.success(
                cartService.getMyCart(page, size)
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<CartItem> updateQuantity(
            @PathVariable Long id,
            @RequestParam Integer quantity
    ) {
        return ApiResponse.success(
                cartService.updateQuantity(id, quantity)
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> removeCartItem(
            @PathVariable Long id
    ) {

        cartService.removeCartItem(id);

        return ApiResponse.success("删除成功");
    }
}
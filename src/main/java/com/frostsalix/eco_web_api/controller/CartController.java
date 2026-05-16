package com.frostsalix.eco_web_api.controller;

import com.frostsalix.eco_web_api.common.ApiResponse;
import com.frostsalix.eco_web_api.dto.AddToCartDTO;
import com.frostsalix.eco_web_api.model.CartItem;
import com.frostsalix.eco_web_api.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@Tag(name = "购物车", description = "购物车增删改查")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    @Operation(summary = "添加商品到购物车")
    public ApiResponse<CartItem> addToCart(
            @RequestBody @Valid AddToCartDTO dto
    ) {

        return ApiResponse.success(
                cartService.addToCart(dto)
        );
    }

    @GetMapping
    @Operation(summary = "查看我的购物车")
    public ApiResponse<Page<CartItem>> getMyCart(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return ApiResponse.success(
                cartService.getMyCart(page, size)
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新购物车商品数量")
    public ApiResponse<CartItem> updateQuantity(
            @PathVariable Long id,
            @RequestParam @Min(1) Integer quantity
    ) {
        return ApiResponse.success(
                cartService.updateQuantity(id, quantity)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "从购物车移除商品")
    public ApiResponse<String> removeCartItem(
            @PathVariable Long id
    ) {

        cartService.removeCartItem(id);

        return ApiResponse.success("删除成功");
    }
}
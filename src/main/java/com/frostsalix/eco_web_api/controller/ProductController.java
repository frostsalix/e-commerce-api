package com.frostsalix.eco_web_api.controller;

import com.frostsalix.eco_web_api.common.ApiResponse;
import com.frostsalix.eco_web_api.dto.ProductDTO;
import com.frostsalix.eco_web_api.dto.ProductResponseDTO;
import com.frostsalix.eco_web_api.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@Tag(name = "商品", description = "商品管理与搜索")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "新增商品（管理员）")
    public ApiResponse<ProductResponseDTO> addProduct(
            @RequestBody @Valid ProductDTO dto
    ) {
        return ApiResponse.success(productService.addProduct(dto));
    }

    @GetMapping
    @Operation(summary = "搜索商品（分页）")
    public ApiResponse<Page<ProductResponseDTO>> getAll(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "最低价格") @RequestParam(required = false) Double minPrice,
            @Parameter(description = "最高价格") @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(
                productService.searchProducts(keyword, minPrice, maxPrice, page, size)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "查看商品详情")
    public ApiResponse<ProductResponseDTO> getById(@PathVariable Long id) {
        return ApiResponse.success(productService.getProductById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新商品（管理员）")
    public ApiResponse<ProductResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid ProductDTO dto
    ) {
        return ApiResponse.success(productService.updateProduct(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除商品（管理员）")
    public ApiResponse<String> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.success("deleted");
    }
}
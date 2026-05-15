package com.frostsalix.eco_web_api.controller;

import com.frostsalix.eco_web_api.common.ApiResponse;
import com.frostsalix.eco_web_api.dto.ProductDTO;
import com.frostsalix.eco_web_api.dto.ProductResponseDTO;
import com.frostsalix.eco_web_api.model.Product;
import com.frostsalix.eco_web_api.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ApiResponse<ProductResponseDTO> addProduct(@RequestBody @Valid ProductDTO dto) {
        return ApiResponse.success(productService.addProduct(dto));
    }

    @GetMapping
    public ApiResponse<Page<ProductResponseDTO>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(
                productService.searchProducts(keyword, minPrice, maxPrice, page, size)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponseDTO> getById(@PathVariable Long id) {
        return ApiResponse.success(productService.getProductById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<Product> update(@PathVariable Long id,
                                       @RequestBody @Valid Product product) {
        return ApiResponse.success(productService.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.success("deleted");
    }
}
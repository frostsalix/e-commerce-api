package com.frostsalix.eco_web_api.service;

import com.frostsalix.eco_web_api.dto.ProductDTO;
import com.frostsalix.eco_web_api.dto.ProductResponseDTO;
import com.frostsalix.eco_web_api.exception.ResourceNotFoundException;
import com.frostsalix.eco_web_api.model.Product;
import com.frostsalix.eco_web_api.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // 新增商品（管理员）
    public ProductResponseDTO addProduct(ProductDTO dto) {

        Product product = new Product();
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());

        Product saved = productRepository.save(product);

        return convertToDTO(saved);
    }
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    // 搜索商品：关键词模糊匹配 + 价格区间 + 分页
    public Page<ProductResponseDTO> searchProducts(
            String keyword,
            Double minPrice,
            Double maxPrice,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Product> specification = buildSearchSpecification(
                keyword,
                minPrice,
                maxPrice
        );

        return productRepository.findAll(specification, pageable)
                .map(this::convertToDTO);
    }

    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        return convertToDTO(product);
    }

    // 更新商品信息（管理员）
    public ProductResponseDTO updateProduct(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());

        return convertToDTO(productRepository.save(product));
    }

    // 删除商品（管理员）
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    private Specification<Product> buildSearchSpecification(
            String keyword,
            Double minPrice,
            Double maxPrice
    ) {
        return (root, query, builder) -> {
            var predicate = builder.conjunction();

            if (keyword != null && !keyword.isBlank()) {
                predicate = builder.and(
                        predicate,
                        builder.like(
                                builder.lower(root.get("name")),
                                "%" + keyword.toLowerCase() + "%"
                        )
                );
            }

            if (minPrice != null) {
                predicate = builder.and(
                        predicate,
                        builder.greaterThanOrEqualTo(root.get("price"), minPrice)
                );
            }

            if (maxPrice != null) {
                predicate = builder.and(
                        predicate,
                        builder.lessThanOrEqualTo(root.get("price"), maxPrice)
                );
            }

            return predicate;
        };
    }

    private ProductResponseDTO convertToDTO(Product product) {

        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());

        return dto;
    }
}
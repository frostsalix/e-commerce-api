package com.frostsalix.eco_web_api.service;

import com.frostsalix.eco_web_api.dto.ProductResponseDTO;
import com.frostsalix.eco_web_api.model.Product;
import com.frostsalix.eco_web_api.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldReturnPagedProductsWhenSearchingWithFilters() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Phone");
        product.setPrice(99.0);
        product.setStock(5);

        Page<Product> productPage = new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(productPage);

        Page<ProductResponseDTO> result = productService.searchProducts(
                "pho",
                10.0,
                100.0,
                0,
                20
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("Phone");
        assertThat(result.getContent().getFirst().getPrice()).isEqualTo(99.0);
    }
}

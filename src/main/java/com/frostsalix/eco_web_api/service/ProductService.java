package com.frostsalix.eco_web_api.service;

import com.frostsalix.eco_web_api.dto.ProductDTO;
import com.frostsalix.eco_web_api.dto.ProductResponseDTO;
import com.frostsalix.eco_web_api.exception.ResourceNotFoundException;
import com.frostsalix.eco_web_api.model.Product;
import com.frostsalix.eco_web_api.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

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

    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        return convertToDTO(product);
    }

    public Product updateProduct(Long id, Product newProduct) {
        Product product = productRepository.findById(id)
                .orElse(null);

        if (product == null) {
            return null;
        }

        product.setName(newProduct.getName());
        product.setPrice(newProduct.getPrice());
        product.setStock(newProduct.getStock());

        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
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
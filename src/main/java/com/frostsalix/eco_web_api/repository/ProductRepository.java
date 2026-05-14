package com.frostsalix.eco_web_api.repository;

import com.frostsalix.eco_web_api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
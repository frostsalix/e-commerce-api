package com.frostsalix.eco_web_api.model;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;

@Data
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "name cannot be empty")
    private String name;

    @NotNull(message = "price cannot be null")
    @Min(value = 0, message = "price must be >= 0")
    private Double price;

    @NotNull(message = "stock cannot be null")
    @Min(value = 0, message = "stock must be >= 0")
    private Integer stock;
}
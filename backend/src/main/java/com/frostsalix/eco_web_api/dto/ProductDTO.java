package com.frostsalix.eco_web_api.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductDTO {

    @NotBlank(message = "name cannot be empty")
    private String name;

    @NotNull
    @Min(value = 0, message = "price must be >= 0")
    private Double price;

    @NotNull
    @Min(value = 0, message = "stock must be >= 0")
    private Integer stock;
}
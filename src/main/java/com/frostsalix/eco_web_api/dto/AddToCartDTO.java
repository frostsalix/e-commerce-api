package com.frostsalix.eco_web_api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddToCartDTO {

    @NotNull(message = "productId must not be null")
    private Long productId;

    @NotNull(message = "quantity must not be null")
    @Min(value = 1, message = "quantity must be >= 1")
    private Integer quantity;
}
package com.frostsalix.eco_web_api.dto;

import lombok.Data;

@Data
public class ProductResponseDTO {

    private Long id;
    private String name;
    private Double price;
    private Integer stock;
}
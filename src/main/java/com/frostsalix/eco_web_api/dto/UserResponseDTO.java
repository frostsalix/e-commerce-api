package com.frostsalix.eco_web_api.dto;

import lombok.Data;

@Data
public class UserResponseDTO {

    private Long id;

    private String username;

    private String role;
}
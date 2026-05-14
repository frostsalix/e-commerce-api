package com.frostsalix.eco_web_api.controller;

import com.frostsalix.eco_web_api.common.ApiResponse;
import com.frostsalix.eco_web_api.dto.LoginDTO;
import com.frostsalix.eco_web_api.dto.LoginResponseDTO;
import com.frostsalix.eco_web_api.dto.RegisterDTO;
import com.frostsalix.eco_web_api.dto.UserResponseDTO;
import com.frostsalix.eco_web_api.model.User;
import com.frostsalix.eco_web_api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ApiResponse<UserResponseDTO> register(@RequestBody @Valid RegisterDTO dto) {

        return ApiResponse.success(userService.register(dto));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponseDTO> login(
            @RequestBody @Valid LoginDTO dto
    ) {

        return ApiResponse.success(userService.login(dto));
    }
}
package com.frostsalix.eco_web_api.controller;

import com.frostsalix.eco_web_api.common.ApiResponse;
import com.frostsalix.eco_web_api.dto.LoginDTO;
import com.frostsalix.eco_web_api.dto.LoginResponseDTO;
import com.frostsalix.eco_web_api.dto.RegisterDTO;
import com.frostsalix.eco_web_api.dto.UserResponseDTO;
import com.frostsalix.eco_web_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "用户", description = "注册与登录")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public ApiResponse<UserResponseDTO> register(
            @RequestBody @Valid RegisterDTO dto
    ) {

        UserResponseDTO user =
                userService.register(dto);

        return ApiResponse.success(user);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public ApiResponse<LoginResponseDTO> login(
            @RequestBody @Valid LoginDTO dto
    ) {

        return ApiResponse.success(userService.login(dto));
    }
}
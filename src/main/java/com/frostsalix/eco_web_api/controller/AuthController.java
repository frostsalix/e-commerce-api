package com.frostsalix.eco_web_api.controller;

import com.frostsalix.eco_web_api.util.JwtUtil;
import com.frostsalix.eco_web_api.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@RequestParam String username,
                                                  @RequestParam String password) {


        // 简化版（真实项目是查数据库）
        if ("admin".equals(username) && "123456".equals(password)) {

            String token = JwtUtil.generateToken(username);

            return ApiResponse.success(Map.of("token", token));
        }

        return new ApiResponse<>(401, "Invalid credentials", null);
    }
}
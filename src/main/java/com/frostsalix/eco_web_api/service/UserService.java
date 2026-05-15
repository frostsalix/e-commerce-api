package com.frostsalix.eco_web_api.service;

import com.frostsalix.eco_web_api.dto.LoginDTO;
import com.frostsalix.eco_web_api.dto.LoginResponseDTO;
import com.frostsalix.eco_web_api.dto.RegisterDTO;
import com.frostsalix.eco_web_api.dto.UserResponseDTO;
import com.frostsalix.eco_web_api.model.User;
import com.frostsalix.eco_web_api.repository.UserRepository;
import com.frostsalix.eco_web_api.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserResponseDTO register(RegisterDTO dto) {

        User user = new User();

        user.setUsername(dto.getUsername());

        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        user.setRole("USER");

        User saved = userRepository.save(user);

        UserResponseDTO response = new UserResponseDTO();

        response.setId(saved.getId());
        response.setUsername(saved.getUsername());
        response.setRole(saved.getRole());

        return response;
    }

    public LoginResponseDTO login(LoginDTO dto) {

        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean matches = passwordEncoder.matches(
                dto.getPassword(),
                user.getPassword()
        );

        if (!matches) {
            throw new RuntimeException("Password incorrect");
        }

        String token =
                JwtUtil.generateToken(
                        user.getUsername(),
                        user.getRole()
                );

        return new LoginResponseDTO(token);
    }
}
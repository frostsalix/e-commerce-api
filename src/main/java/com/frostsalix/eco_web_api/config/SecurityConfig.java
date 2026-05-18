package com.frostsalix.eco_web_api.config;

import com.frostsalix.eco_web_api.config.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/users/login",
                                "/users/register",
                                "/payment/webhook",
                                "/admin/login",
                                "/css/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/products/**")
                        .hasAnyRole("USER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/products",
                                "/products/**"
                        ).hasAnyRole("USER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/products",
                                "/products/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/products",
                                "/products/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/products",
                                "/products/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/orders/*/ship",
                                "/orders/*/deliver"
                        ).hasRole("ADMIN")

                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        new JwtFilter(),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
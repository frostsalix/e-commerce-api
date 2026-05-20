package com.frostsalix.eco_web_api.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET = requireJwtSecret();

    private static final Key KEY =
            Keys.hmacShaKeyFor(SECRET.getBytes());

    private static String requireJwtSecret() {
        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.isBlank()) {
            secret = "01234567890123456789012345678901";
        }
        if (secret.length() < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must be at least 32 characters"
            );
        }
        return secret;
    }

    public static String generateToken(
            String username,
            String role
    ) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + 3600000)
                )
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // JwtUtil 添加解析方法
    public static String extractUsername(String token) {

        return Jwts.parser()
                .verifyWith((SecretKey) KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // 添加解析 role 方法
    public static String extractRole(String token) {

        return Jwts.parser()
                .verifyWith((SecretKey) KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }
}
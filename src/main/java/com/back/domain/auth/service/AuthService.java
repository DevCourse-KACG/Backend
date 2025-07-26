package com.back.domain.auth.service;

import com.back.global.config.jwt.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class AuthService {
    private final Key secretKey;
    private final long expirationSeconds;

    public AuthService(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
        this.expirationSeconds = Long.parseLong(jwtProperties.getExpirationSeconds());
    }

    public String generateAccessToken(String validApiKey) {
        Date now = new Date();
        Date expired = new Date(now.getTime() + expirationSeconds * 1000);

        return "Bearer " + Jwts.builder()
                .setSubject("api-auth")
                .claim("api-auth", validApiKey)
                .setIssuedAt(now)
                .setExpiration(expired)
                .signWith(secretKey)
                .compact();
    }
}

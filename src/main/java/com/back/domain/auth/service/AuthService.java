package com.back.domain.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class AuthService {
    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateAccessToken(String validApiKey) {
    Date now =  new Date();
    Date expired = new Date(now.getTime() + 1000 *  60 * 60);

    return "Bearer " + Jwts.builder()
            .setSubject("api-auth")
            .claim("api-auth",  validApiKey)
            .setIssuedAt(now)
            .setExpiration(expired)
            .signWith(secretKey)
            .compact();
    }
}

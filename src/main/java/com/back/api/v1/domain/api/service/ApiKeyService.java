package com.back.api.v1.domain.api.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ApiKeyService {
    public String generateApiKey(Long userId) {
        return "api_" + UUID.randomUUID().toString().replace("-", "");
    }
}

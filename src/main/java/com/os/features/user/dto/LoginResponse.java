package com.os.features.user.dto;

public record LoginResponse(
        String token,
        String type,
        long expiresIn
) {}
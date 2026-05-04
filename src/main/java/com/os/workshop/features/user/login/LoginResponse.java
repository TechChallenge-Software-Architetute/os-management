package com.os.workshop.features.user.login;

public record LoginResponse(
        String token,
        String type,
        long expiresIn
) {}

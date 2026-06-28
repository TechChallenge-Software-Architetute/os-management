package com.os.workshop.adapter.in.web.user;

public record LoginResponse(String token, String type, long expiresIn) {}

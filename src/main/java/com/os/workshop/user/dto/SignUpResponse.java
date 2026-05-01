package com.os.workshop.user.dto;

import java.util.Set;
import java.util.UUID;

public record SignUpResponse(
        UUID id,
        String email,
        Set<String> roles
) {}
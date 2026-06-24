package com.os.workshop.features.user.signUp;

import java.util.Set;
import java.util.UUID;

public record SignUpResponse(
        UUID id,
        String email,
        Set<String> roles
) {}

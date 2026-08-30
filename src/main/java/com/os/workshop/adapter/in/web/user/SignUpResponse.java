package com.os.workshop.adapter.in.web.user;

import com.os.workshop.application.user.port.out.UserRepository;

import java.util.Set;
import java.util.UUID;

public record SignUpResponse(UUID id, String email, String cpf, Set<String> roles) {
    public static SignUpResponse from(UserRepository.SignUpResult result) {
        return new SignUpResponse(result.id(), result.email(), result.cpf(), result.roles());
    }
}

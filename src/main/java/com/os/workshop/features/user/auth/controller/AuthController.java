package com.os.workshop.features.user.auth.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.Operation;

import com.os.workshop.features.user.auth.iterator.AuthIterator;
import com.os.workshop.features.user.dto.LoginRequest;
import com.os.workshop.features.user.dto.LoginResponse;
import com.os.workshop.features.user.security.config.JwtProperties;
import com.os.workshop.features.user.security.jwt.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Authenticate users and issue JWT tokens.")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final JwtProperties  jwtProperties;
    private final AuthIterator authIterator;

    @Operation(summary = "Authenticate user", description = "Authenticate user endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Request payload for this operation",
        required = true,
        content = @Content(schema = @Schema(implementation = LoginRequest.class))
)
@RequestBody @Valid LoginRequest request) {

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        String token = jwtService.generateToken(authIterator.getUserByEmail(request.email()));

        var result = new LoginResponse(
                token,
                "Bearer",
                jwtProperties.getExpiration()
        );
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}

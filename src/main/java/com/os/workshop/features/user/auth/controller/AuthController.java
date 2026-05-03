package com.os.workshop.features.user.auth.controller;

import com.os.workshop.features.common.api.ErrorResponse;
import com.os.workshop.features.user.auth.iterator.AuthIterator;
import com.os.workshop.features.user.dto.LoginRequest;
import com.os.workshop.features.user.dto.LoginResponse;
import com.os.workshop.features.user.security.config.JwtProperties;
import com.os.workshop.features.user.security.jwt.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authenticate users and issue JWT access tokens.")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final JwtProperties  jwtProperties;
    private final AuthIterator authIterator;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates a user with email and password and returns a JWT bearer token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LoginResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User credentials.", required = true, content = @Content(schema = @Schema(implementation = LoginRequest.class)))
            @RequestBody @Valid LoginRequest request) {
        logger.info("Login requested. email={}", request.email());

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
        logger.info("Login succeeded. email={}", request.email());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}

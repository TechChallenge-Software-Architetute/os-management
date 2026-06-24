package com.os.workshop.features.user;

import com.os.workshop.features.user.login.LoginHandler;
import com.os.workshop.features.user.login.LoginRequest;
import com.os.workshop.features.user.login.LoginResponse;
import com.os.workshop.features.user.signUp.SignUpHandler;
import com.os.workshop.features.user.signUp.SignUpRequest;
import com.os.workshop.features.user.signUp.SignUpResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authenticate users and create user accounts.")
public class UserController {

    private final LoginHandler loginHandler;
    private final SignUpHandler signUpHandler;

    @Operation(summary = "Authenticate user", description = "Authenticates a user and returns an access token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequest.class))
            )
            @RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(loginHandler.handle(request));
    }

    @Operation(summary = "Create user", description = "Creates a new user account with the provided roles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User account data.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SignUpRequest.class))
            )
            @RequestBody @Valid SignUpRequest request) {
        return ResponseEntity.ok(signUpHandler.handle(request));
    }
}

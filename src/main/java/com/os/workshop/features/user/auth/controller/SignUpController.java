package com.os.workshop.features.user.auth.controller;

import com.os.workshop.features.common.api.ErrorResponse;
import com.os.workshop.features.user.auth.iterator.SignUpIterator;
import com.os.workshop.features.user.dto.SignUpRequest;
import com.os.workshop.features.user.dto.SignUpResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("signup")
@Tag(name = "Users", description = "Manage application user accounts and roles.")
public class SignUpController {

    private static final Logger logger = LoggerFactory.getLogger(SignUpController.class);

    private final SignUpIterator signUpIterator;


    public SignUpController(SignUpIterator signUpIterator) {
        this.signUpIterator = signUpIterator;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create user account", description = "Creates a new application user account. Requires an administrator role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User account created successfully", content = @Content(schema = @Schema(implementation = SignUpResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Current user is not allowed to create accounts", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SignUpResponse> signUp(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User account data to create.", required = true, content = @Content(schema = @Schema(implementation = SignUpRequest.class)))
            @Valid @RequestBody SignUpRequest signUpRequest) {
        logger.info("Sign-up requested. email={}, roles={}", signUpRequest.email(), signUpRequest.roles());
        var response = signUpIterator.signUp(signUpRequest);
        logger.info("User account created. id={}", response.id());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

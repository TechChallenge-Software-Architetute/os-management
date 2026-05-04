package com.os.workshop.features.user.auth.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.Operation;

import com.os.workshop.features.user.auth.iterator.SignUpIterator;
import com.os.workshop.features.user.dto.SignUpRequest;
import com.os.workshop.features.user.dto.SignUpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "Create application users.")
@RestController
@RequestMapping("signup")
public class SignUpController {

    private final SignUpIterator signUpIterator;


    public SignUpController(SignUpIterator signUpIterator) {
        this.signUpIterator = signUpIterator;
    }

    @Operation(summary = "Create user", description = "Create user endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<SignUpResponse> signUp(@io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Request payload for this operation",
        required = true,
        content = @Content(schema = @Schema(implementation = SignUpRequest.class))
)
@RequestBody SignUpRequest signUpRequest) {
        return new ResponseEntity<>(signUpIterator.signUp(signUpRequest), HttpStatus.OK);
    }
}

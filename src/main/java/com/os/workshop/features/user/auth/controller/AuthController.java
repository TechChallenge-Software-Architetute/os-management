package com.os.workshop.features.user.auth.controller;

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

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final JwtProperties  jwtProperties;
    private final AuthIterator authIterator;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {

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
package com.os.workshop.features.user;

import com.os.workshop.features.user.login.LoginHandler;
import com.os.workshop.features.user.login.LoginRequest;
import com.os.workshop.features.user.login.LoginResponse;
import com.os.workshop.features.user.signUp.SignUpHandler;
import com.os.workshop.features.user.signUp.SignUpRequest;
import com.os.workshop.features.user.signUp.SignUpResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final LoginHandler loginHandler;
    private final SignUpHandler signUpHandler;

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(loginHandler.handle(request));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody @Valid SignUpRequest request) {
        return ResponseEntity.ok(signUpHandler.handle(request));
    }
}

package com.os.workshop.adapter.in.web.user;

import com.os.workshop.application.user.LoginUseCase;
import com.os.workshop.application.user.SignUpUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final LoginUseCase loginUseCase;
    private final SignUpUseCase signUpUseCase;

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        var result = loginUseCase.execute(request.login(), request.password());
        return ResponseEntity.ok(new LoginResponse(result.token(), result.type(), result.expiresIn()));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody @Valid SignUpRequest request) {
        var result = signUpUseCase.execute(request.email(), request.cpf(), request.password(), request.roles());
        return ResponseEntity.ok(SignUpResponse.from(result));
    }
}

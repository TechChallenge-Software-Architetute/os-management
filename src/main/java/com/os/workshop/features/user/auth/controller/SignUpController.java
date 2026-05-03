package com.os.workshop.features.user.auth.controller;

import com.os.workshop.features.user.auth.iterator.SignUpIterator;
import com.os.workshop.features.user.dto.SignUpRequest;
import com.os.workshop.features.user.dto.SignUpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("signup")
public class SignUpController {

    private final SignUpIterator signUpIterator;


    public SignUpController(SignUpIterator signUpIterator) {
        this.signUpIterator = signUpIterator;
    }

    @PostMapping
    @PreAuthorize("ROLE_ADMIN")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody SignUpRequest signUpRequest) {
        return new ResponseEntity<>(signUpIterator.signUp(signUpRequest), HttpStatus.OK);
    }
}

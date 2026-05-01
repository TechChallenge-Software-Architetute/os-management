package com.os.workshop.user.repository;

import com.os.workshop.user.dto.SignUpRequest;
import com.os.workshop.user.dto.SignUpResponse;

public interface SignUpRepository {
    SignUpResponse signUp(SignUpRequest signUpRequest);
}

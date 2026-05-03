package com.os.workshop.features.user.repository;

import com.os.workshop.features.user.dto.SignUpRequest;
import com.os.workshop.features.user.dto.SignUpResponse;

public interface SignUpRepository {
    SignUpResponse signUp(SignUpRequest signUpRequest);
}

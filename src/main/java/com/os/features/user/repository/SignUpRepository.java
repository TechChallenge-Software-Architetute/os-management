package com.os.features.user.repository;

import com.os.features.user.dto.SignUpRequest;
import com.os.features.user.dto.SignUpResponse;

public interface SignUpRepository {
    SignUpResponse signUp(SignUpRequest signUpRequest);
}

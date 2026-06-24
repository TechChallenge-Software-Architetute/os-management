package com.os.workshop.features.user;

import com.os.workshop.features.user.login.LoginHandler;
import com.os.workshop.features.user.login.LoginRequest;
import com.os.workshop.features.user.login.LoginResponse;
import com.os.workshop.features.user.signUp.SignUpHandler;
import com.os.workshop.features.user.signUp.SignUpRequest;
import com.os.workshop.features.user.signUp.SignUpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private final LoginHandler loginHandler = mock(LoginHandler.class);
    private final SignUpHandler signUpHandler = mock(SignUpHandler.class);
    private final UserController controller = new UserController(loginHandler, signUpHandler);

    @Test
    void loginReturns200() {
        var request = new LoginRequest("user@test.com", "pass");
        when(loginHandler.handle(request)).thenReturn(new LoginResponse("token", "Bearer", 3600));
        assertEquals(HttpStatus.OK, controller.login(request).getStatusCode());
    }

    @Test
    void signUpReturns200() {
        var request = new SignUpRequest("user@test.com", "pass", Set.of("USER"));
        when(signUpHandler.handle(request)).thenReturn(new SignUpResponse(UUID.randomUUID(), "user@test.com", Set.of("USER")));
        assertEquals(HttpStatus.OK, controller.signUp(request).getStatusCode());
    }
}

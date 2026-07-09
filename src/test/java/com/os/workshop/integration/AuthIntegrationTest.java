package com.os.workshop.integration;

import com.os.workshop.integration.config.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AuthIntegrationTest extends IntegrationTestBase {

    @Test
    void should_signup_and_login_successfully() {
        // Sign up
        var signUpBody = Map.of(
                "email", "auth-test@workshop.com",
                "password", "Secure@123",
                "roles", Set.of("ADMIN")
        );
        var signUpResponse = restTemplate.postForEntity("/signup", signUpBody, Map.class);
        assertThat(signUpResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(signUpResponse.getBody()).containsKey("id");
        assertThat(signUpResponse.getBody().get("email")).isEqualTo("auth-test@workshop.com");

        // Login
        var loginBody = Map.of("email", "auth-test@workshop.com", "password", "Secure@123");
        var loginResponse = restTemplate.postForEntity("/auth/login", loginBody, Map.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).containsKey("token");
        assertThat(loginResponse.getBody().get("type")).isEqualTo("Bearer");
        assertThat((Number) loginResponse.getBody().get("expiresIn")).isNotNull();

        // Use token to access a protected endpoint
        String token = (String) loginResponse.getBody().get("token");
        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        var protectedResponse = restTemplate.exchange(
                "/api/clients", HttpMethod.GET, new HttpEntity<>(headers), Object.class);
        assertThat(protectedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}

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
                "cpf", "52998224725",
                "password", "Secure@123",
                "roles", Set.of("ADMIN")
        );
        var signUpResponse = restTemplate.postForEntity("/signup", signUpBody, Map.class);
        assertThat(signUpResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(signUpResponse.getBody()).containsKey("id");
        assertThat(signUpResponse.getBody().get("email")).isEqualTo("auth-test@workshop.com");
        assertThat(signUpResponse.getBody().get("cpf")).isEqualTo("52998224725");

        // Login with email
        var loginBody = Map.of("login", "auth-test@workshop.com", "password", "Secure@123");
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

    @Test
    void should_signup_and_login_with_cpf() {
        var signUpBody = Map.of(
                "email", "cpf-login-test@workshop.com",
                "cpf", "111.444.777-35",
                "password", "Secure@123",
                "roles", Set.of("ADMIN")
        );
        var signUpResponse = restTemplate.postForEntity("/signup", signUpBody, Map.class);
        assertThat(signUpResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Login using the CPF (with punctuation) instead of the email
        var loginBody = Map.of("login", "111.444.777-35", "password", "Secure@123");
        var loginResponse = restTemplate.postForEntity("/auth/login", loginBody, Map.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).containsKey("token");
    }

    @Test
    void should_reject_signup_without_cpf() {
        var signUpBody = Map.of(
                "email", "no-cpf-test@workshop.com",
                "password", "Secure@123",
                "roles", Set.of("ADMIN")
        );
        var signUpResponse = restTemplate.postForEntity("/signup", signUpBody, Map.class);
        assertThat(signUpResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_return_generic_error_for_unknown_cpf_login() {
        var loginBody = Map.of("login", "529.982.247-25", "password", "whatever");
        var loginResponse = restTemplate.postForEntity("/auth/login", loginBody, Map.class);
        assertThat(loginResponse.getStatusCode().is2xxSuccessful()).isFalse();
    }
}

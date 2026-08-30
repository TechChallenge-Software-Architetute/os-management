package com.os.workshop.application.user;

import com.os.workshop.application.user.port.out.UserRepository;
import com.os.workshop.domain.shared.Cpf;
import com.os.workshop.domain.user.User;
import com.os.workshop.infrastructure.security.JwtProperties;
import com.os.workshop.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private static final Pattern NON_DIGIT = Pattern.compile("[^0-9]");

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;

    /**
     * @param login email ou CPF do usuário — o formato é detectado automaticamente.
     */
    public LoginResult execute(String login, String password) {
        String email = resolveEmail(login);

        authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

        User user = userRepository.findByEmail(email).orElseThrow();
        user.setPassword(null);

        String token = jwtService.generateToken(user);
        return new LoginResult(token, "Bearer", jwtProperties.getExpiration());
    }

    /**
     * O Spring Security (AuthenticationManager, UserDetailsService, JWT subject) sempre trabalha
     * com o email como principal. Quando o login informado é um CPF, resolvemos para o email do
     * dono antes de autenticar, mantendo o restante do fluxo de segurança inalterado.
     */
    private String resolveEmail(String login) {
        String digitsOnly = NON_DIGIT.matcher(login).replaceAll("");
        if (digitsOnly.length() != 11) {
            return login; // não tem cara de CPF, trata como email
        }

        String normalizedCpf;
        try {
            normalizedCpf = new Cpf(digitsOnly).getValue();
        } catch (IllegalArgumentException e) {
            // CPF mal formado (dígito verificador inválido): mesma mensagem genérica de credenciais
            // inválidas, para não revelar detalhes de validação a quem está tentando logar.
            throw new BadCredentialsException("Invalid credentials");
        }

        return userRepository.findByCpf(normalizedCpf)
                .map(User::getEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    }

    public record LoginResult(String token, String type, long expiresIn) {}
}

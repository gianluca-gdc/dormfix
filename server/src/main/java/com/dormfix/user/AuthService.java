package com.dormfix.user;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private static final String INVALID_CREDENTIALS = "Invalid email or password";

    private final UserRepository users;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        if (request == null || request.email() == null || request.password() == null) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS);
        }

        Optional<User> user = users.findByEmail(request.email());
        if (user.isEmpty() || !passwordEncoder.matches(request.password(), user.get().getPasswordHash())) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS);
        }

        User account = user.get();
        return new LoginResponse(account.getId(), account.getName(), account.getRole(), account.getRoomNumber());
    }
}

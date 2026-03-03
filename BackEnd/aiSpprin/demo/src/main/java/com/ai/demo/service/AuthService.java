package com.ai.demo.service;

import com.ai.demo.dto.AuthResponse;
import com.ai.demo.dto.LoginRequest;
import com.ai.demo.dto.RegisterRequest;
import com.ai.demo.model.User;
import com.ai.demo.repository.UserRepository;
import com.ai.demo.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.jwt.expiration}")
    private long jwtExpiration;

    public AuthResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : "ROLE_USER")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getUsername(), java.util.List.of(user.getRole()));
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .expiresIn(jwtExpiration)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (!user.getEnabled()) {
            throw new RuntimeException("Account is disabled");
        }

        String token = jwtService.generateToken(user.getUsername(), java.util.List.of(user.getRole()));
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .expiresIn(jwtExpiration)
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        String username = jwtService.extractUsername(refreshToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!jwtService.isTokenValid(refreshToken, username)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String newToken = jwtService.generateToken(user.getUsername(), java.util.List.of(user.getRole()));
        String newRefreshToken = jwtService.generateRefreshToken(user.getUsername());

        return AuthResponse.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .expiresIn(jwtExpiration)
                .build();
    }
}

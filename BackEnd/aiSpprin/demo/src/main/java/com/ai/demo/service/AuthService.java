package com.ai.demo.service;

import com.ai.demo.dto.AuthResponse;
import com.ai.demo.dto.LoginRequest;
import com.ai.demo.dto.RegisterRequest;
import com.ai.demo.model.User;
import com.ai.demo.repository.UserRepository;
import com.ai.demo.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.jwt.expiration}")
    private long jwtExpiration;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .name(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : "ROLE_USER")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getName(), List.of(user.getRole()));
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        log.info("User registered successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .username(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .expiresIn(jwtExpiration)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (!user.isEnabled()) {
            throw new RuntimeException("Account is disabled");
        }

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getName(), List.of(user.getRole()));
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        log.info("User logged in successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .username(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .expiresIn(jwtExpiration)
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtService.extractUsername(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!jwtService.isTokenValid(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String newToken = jwtService.generateToken(user.getEmail(), user.getName(), List.of(user.getRole()));
        String newRefreshToken = jwtService.generateRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .username(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .expiresIn(jwtExpiration)
                .build();
    }

    @Transactional
    public User findOrCreateOAuthUser(String email, String name, String googleId, String pictureUrl) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .name(name)
                            .googleId(googleId)
                            .pictureUrl(pictureUrl)
                            .role("ROLE_USER")
                            .isActive(true)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .lastLogin(LocalDateTime.now())
                            .build();
                    log.info("Creating new OAuth user: {}", email);
                    return userRepository.save(newUser);
                });
    }
}

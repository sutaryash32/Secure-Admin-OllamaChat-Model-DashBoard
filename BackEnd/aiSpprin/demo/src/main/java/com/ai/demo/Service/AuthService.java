package com.ai.demo.Service;

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
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.jwt.expiration}")
    private long jwtExpiration;

    public Mono<AuthResponse> register(RegisterRequest request) {
        // Check if username already exists
        return userRepository.existsByUsername(request.getUsername())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("Username already exists"));
                    }
                    return userRepository.existsByEmail(request.getEmail());
                })
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("Email already exists"));
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

                    return userRepository.save(user);
                })
                .flatMap(user -> {
                    String token = jwtService.generateToken(user.getUsername(), java.util.List.of(user.getRole()));
                    String refreshToken = jwtService.generateRefreshToken(user.getUsername());

                    AuthResponse response = AuthResponse.builder()
                            .token(token)
                            .refreshToken(refreshToken)
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .role(user.getRole())
                            .expiresIn(jwtExpiration)
                            .build();

                    return Mono.just(response);
                });
    }

    public Mono<AuthResponse> login(LoginRequest request) {
        return userRepository.findByUsername(request.getUsername())
                .flatMap(user -> {
                    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        return Mono.error(new RuntimeException("Invalid credentials"));
                    }

                    if (!user.getEnabled()) {
                        return Mono.error(new RuntimeException("Account is disabled"));
                    }

                    String token = jwtService.generateToken(user.getUsername(), java.util.List.of(user.getRole()));
                    String refreshToken = jwtService.generateRefreshToken(user.getUsername());

                    AuthResponse response = AuthResponse.builder()
                            .token(token)
                            .refreshToken(refreshToken)
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .role(user.getRole())
                            .expiresIn(jwtExpiration)
                            .build();

                    return Mono.just(response);
                });
    }

    public Mono<AuthResponse> refreshToken(String refreshToken) {
        String username = jwtService.extractUsername(refreshToken);

        return userRepository.findByUsername(username)
                .flatMap(user -> {
                    if (!jwtService.isTokenValid(refreshToken, username)) {
                        return Mono.error(new RuntimeException("Invalid refresh token"));
                    }

                    String newToken = jwtService.generateToken(user.getUsername(), java.util.List.of(user.getRole()));
                    String newRefreshToken = jwtService.generateRefreshToken(user.getUsername());

                    AuthResponse response = AuthResponse.builder()
                            .token(newToken)
                            .refreshToken(newRefreshToken)
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .role(user.getRole())
                            .expiresIn(jwtExpiration)
                            .build();

                    return Mono.just(response);
                });
    }
}

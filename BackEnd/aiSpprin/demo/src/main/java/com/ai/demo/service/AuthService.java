package com.ai.demo.service;

import com.ai.demo.dto.AuthResponseDto;
import com.ai.demo.dto.LoginRequestDto;
import com.ai.demo.dto.RegisterRequestDto;
import com.ai.demo.exception.AppException;
import com.ai.demo.model.User;
import com.ai.demo.repository.UserRepository;
import com.ai.demo.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.expiration-ms}")          // was ${spring.jwt.expiration}
    private long jwtExpirationMs;

    @Transactional
    public Mono<AuthResponseDto> register(RegisterRequestDto request) {
        return Mono.fromCallable(() -> {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new AppException("Email already exists", HttpStatus.CONFLICT);
            }

            User user = User.builder()
                    .email(request.getEmail())
                    .name(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(request.getRole() != null ? request.getRole() : "ROLE_USER")
                    .isActive(true)
                    .build();

            user = userRepository.save(user);
            log.info("User registered successfully: {}", user.getEmail());
            return buildAuthResponse(user);

        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<AuthResponseDto> login(LoginRequestDto request) {
        return Mono.fromCallable(() -> {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new AppException("Invalid credentials", HttpStatus.UNAUTHORIZED));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new AppException("Invalid credentials", HttpStatus.UNAUTHORIZED);
            }
            if (!user.isEnabled()) {
                throw new AppException("Account is disabled", HttpStatus.FORBIDDEN);
            }

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            log.info("User logged in successfully: {}", user.getEmail());
            return buildAuthResponse(user);

        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<AuthResponseDto> refreshToken(String refreshToken) {
        return Mono.fromCallable(() -> {
            if (!jwtService.isValid(refreshToken)) {            // was isTokenValid()
                throw new AppException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
            }

            String email = jwtService.extractEmail(refreshToken); // was extractUsername()
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

            return buildAuthResponse(user);

        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<User> findOrCreateOAuthUser(String email, String name,
                                            String googleId, String pictureUrl) {
        return Mono.fromCallable(() ->
                userRepository.findByEmail(email).orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .name(name)
                            .googleId(googleId)
                            .pictureUrl(pictureUrl)
                            .role("ROLE_USER")
                            .isActive(true)
                            .lastLogin(LocalDateTime.now())
                            .build();
                    log.info("Creating new OAuth user: {}", email);
                    return userRepository.save(newUser);
                })
        ).subscribeOn(Schedulers.boundedElastic());
    }

    // ── private helper ────────────────────────────────────────

    private AuthResponseDto buildAuthResponse(User user) {
        String token = jwtService.generateToken(
                user.getEmail(), user.getName(), List.of(user.getRole()));
        String refresh = jwtService.generateRefreshToken(user.getEmail());

        return AuthResponseDto.builder()
                .token(token)
                .refreshToken(refresh)
                .username(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .expiresIn(jwtExpirationMs)
                .build();
    }
}
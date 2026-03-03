package com.ai.demo.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        log.info("OAuth2 login successful for principal: {}", authentication.getPrincipal());

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Extract user information
        String email = oAuth2User.getAttribute("email");
        String name  = oAuth2User.getAttribute("name");   // display name from Google
        String role  = "ROLE_USER";

        // generateToken(email, name, roles) — matches JwtService signature
        String token        = jwtService.generateToken(email, name, List.of(role));
        String refreshToken = jwtService.generateRefreshToken(email);

        log.info("Generated JWT for OAuth2 user: email={}", email);

        String redirectUrl = String.format(
                "http://localhost:4200/oauth2/callback?token=%s&refreshToken=%s&username=%s&role=%s&email=%s",
                token,
                refreshToken,
                name  != null ? name  : "",
                role,
                email != null ? email : ""
        );

        log.info("Redirecting to: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
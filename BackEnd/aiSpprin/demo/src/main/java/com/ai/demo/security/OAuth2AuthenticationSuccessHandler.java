package com.ai.demo.security;

import com.ai.demo.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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
        String username = oAuth2User.getAttribute("username");
        String email = oAuth2User.getAttribute("email");
        String role = "ROLE_USER"; // Default role for OAuth users
        
        // Generate JWT token
        String token = jwtService.generateToken(username, List.of(role));
        String refreshToken = jwtService.generateRefreshToken(username);
        
        log.info("Generated JWT for OAuth2 user: username={}", username);
        
        // Build redirect URL with tokens
        String redirectUrl = String.format(
                "http://localhost:4200/oauth2/callback?token=%s&refreshToken=%s&username=%s&role=%s&email=%s",
                token,
                refreshToken,
                username,
                role,
                email != null ? email : ""
        );
        
        log.info("Redirecting to: {}", redirectUrl);
        
        // Redirect to frontend
        response.sendRedirect(redirectUrl);
    }
}

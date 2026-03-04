package com.ai.demo.security;

import com.ai.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final AuthService authService;
    private final SuperAdminChecker superAdminChecker;     // ← inject checker, not SecurityConfig

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange,
                                              Authentication authentication) {
        var principal = (OidcUser) authentication.getPrincipal();

        String email      = principal.getEmail();
        String name       = principal.getFullName() != null
                ? principal.getFullName()
                : principal.getAttribute("name");
        String googleId   = principal.getSubject();
        String pictureUrl = principal.getAttribute("picture");

        log.info("OAuth2 login success — email={}, name={}", email, name);

        return authService.findOrCreateOAuthUser(email, name, googleId, pictureUrl)
                .flatMap(user -> {
                    String role = superAdminChecker.isSuperAdmin(email)  // ← use checker
                            ? "ROLE_SUPER_ADMIN"
                            : user.getRole();

                    String token        = jwtService.generateToken(email, name, List.of(role));
                    String refreshToken = jwtService.generateRefreshToken(email);

                    String redirectUrl = frontendUrl + "/oauth2/callback"
                            + "?token="        + encode(token)
                            + "&refreshToken=" + encode(refreshToken)
                            + "&username="     + encode(name  != null ? name  : "")
                            + "&email="        + encode(email != null ? email : "")
                            + "&role="         + encode(role);

                    log.info("Redirecting — email={}, role={}", email, role);

                    var response = webFilterExchange.getExchange().getResponse();
                    response.setStatusCode(HttpStatus.FOUND);
                    response.getHeaders().setLocation(URI.create(redirectUrl));
                    return response.setComplete();
                });
    }

    private String encode(String value) {
        return URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8);
    }
}
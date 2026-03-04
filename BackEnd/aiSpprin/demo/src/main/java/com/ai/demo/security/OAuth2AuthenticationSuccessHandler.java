// BackEnd/aiSpprin/demo/src/main/java/com/ai/demo/security/OAuth2AuthenticationSuccessHandler.java
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

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${app.admin.email-domains}")
    private String adminEmailDomains;

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
                    List<String> roles = isAdminUser(email)
                            ? List.of("ROLE_USER", "ROLE_ADMIN")
                            : List.of("ROLE_USER");

                    String token        = jwtService.generateToken(email, name, roles);
                    String refreshToken = jwtService.generateRefreshToken(email);

                    String redirectUrl = frontendUrl + "/oauth2/callback"
                            + "?token="        + encode(token)
                            + "&refreshToken=" + encode(refreshToken)
                            + "&username="     + encode(name  != null ? name  : "")
                            + "&email="        + encode(email != null ? email : "")
                            + "&role="         + encode(isAdminUser(email) ? "ROLE_ADMIN" : "ROLE_USER");

                    log.info("Redirecting OAuth2 user to frontend callback");

                    var response = webFilterExchange.getExchange().getResponse();
                    response.setStatusCode(HttpStatus.FOUND);
                    response.getHeaders().setLocation(URI.create(redirectUrl));
                    return response.setComplete();
                });
    }

    private boolean isAdminUser(String email) {
        if (email == null) return false;
        for (String domain : adminEmailDomains.split(",")) {
            if (email.toLowerCase().endsWith("@" + domain.trim().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String encode(String value) {
        return URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8);
    }
}
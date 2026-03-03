package com.ai.demo.security;

import com.ai.demo.security.JwtAuthenticationFilter;
import com.ai.demo.security.JwtService;
import com.ai.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final JwtService jwtService;

    @Lazy  // ✅ breaks the circular dependency
    private final AuthService authService;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${app.admin.email-domains}")
    private String adminEmailDomains;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .addFilterBefore(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(ex -> ex
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .pathMatchers("/api/v1/auth/**").permitAll()
                        .pathMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .pathMatchers("/api/v1/chat/**").authenticated()
                        .anyExchange().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authenticationSuccessHandler(oauth2SuccessHandler())
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }

    // ✅ Registered as a @Bean — Spring picks it up automatically for oauth2Login
    @Bean
    public ReactiveOAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        OidcReactiveOAuth2UserService delegate = new OidcReactiveOAuth2UserService();
        return userRequest -> delegate.loadUser(userRequest).map(oidcUser -> {
            String email = oidcUser.getEmail();
            List<SimpleGrantedAuthority> authorities = isAdminUser(email)
                    ? List.of(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"))
                    : Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
            return (OidcUser) new DefaultOidcUser(
                    authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), "sub"
            );
        });
    }

    // ✅ Reactive success handler
    private ServerAuthenticationSuccessHandler oauth2SuccessHandler() {
        return (webFilterExchange, authentication) -> {
            var principal = (OidcUser) authentication.getPrincipal();
            String email      = principal.getEmail();
            String name       = principal.getFullName() != null
                    ? principal.getFullName()
                    : principal.getAttribute("name");
            String googleId   = principal.getSubject();
            String pictureUrl = principal.getAttribute("picture");

            log.info("=== OAuth2 Raw Attributes ===");
            log.info("Attributes: {}", principal.getAttributes());
            log.info("Email: {}, Name: {}, GoogleId: {}", email, name, googleId);

            return authService.findOrCreateOAuthUser(email, name, googleId, pictureUrl)
                    .flatMap(user -> {
                        log.info("=== OAuth2 Login ===");
                        log.info("Name     : {}", user.getName());
                        log.info("Email    : {}", user.getEmail());
                        log.info("GoogleId : {}", user.getGoogleId());
                        log.info("Role     : {}", isAdminUser(email) ? "ROLE_ADMIN" : "ROLE_USER");
                        log.info("===================");

                        List<String> roles = isAdminUser(email)
                                ? List.of("ROLE_USER", "ROLE_ADMIN")
                                : List.of("ROLE_USER");

                        String token        = jwtService.generateToken(email, name, roles);
                        String refreshToken = jwtService.generateRefreshToken(email);

                        String redirectUrl = "http://localhost:4200/oauth2/callback"
                                + "?token="        + encode(token)
                                + "&refreshToken=" + encode(refreshToken)
                                + "&username="     + encode(name  != null ? name  : "")
                                + "&email="        + encode(email != null ? email : "")
                                + "&role="         + encode(isAdminUser(email) ? "ROLE_ADMIN" : "ROLE_USER");

                        var response = webFilterExchange.getExchange().getResponse();
                        response.getHeaders().setLocation(URI.create(redirectUrl));
                        response.setStatusCode(org.springframework.http.HttpStatus.FOUND);
                        return response.setComplete();
                    });
        };
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Content-Type"));
        config.setAllowCredentials(true);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // PasswordEncoder moved to AppConfig.java to break circular dependency
}
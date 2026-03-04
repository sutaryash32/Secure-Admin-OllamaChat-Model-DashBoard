package com.ai.demo.security;

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
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

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
    private final OAuth2AuthenticationSuccessHandler oauth2SuccessHandler;

    @Lazy
    private final AuthService authService;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${app.super-admin.email-domains}")
    private String superAdminEmailDomains;

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
                        .pathMatchers("/api/v1/super-admin/**").hasRole("SUPER_ADMIN")
                        .pathMatchers("/api/v1/chat/**").authenticated()
                        .anyExchange().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authenticationSuccessHandler(oauth2SuccessHandler)
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }

    @Bean
    public ReactiveOAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        OidcReactiveOAuth2UserService delegate = new OidcReactiveOAuth2UserService();
        return userRequest -> delegate.loadUser(userRequest).map(oidcUser -> {
            String email = oidcUser.getEmail();
            List<SimpleGrantedAuthority> authorities = isSuperAdmin(email)
                    ? List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))
                    : Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
            return (OidcUser) new DefaultOidcUser(
                    authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), "sub"
            );
        });
    }

    public boolean isSuperAdmin(String email) {
        if (email == null) return false;
        for (String domain : superAdminEmailDomains.split(",")) {
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
}
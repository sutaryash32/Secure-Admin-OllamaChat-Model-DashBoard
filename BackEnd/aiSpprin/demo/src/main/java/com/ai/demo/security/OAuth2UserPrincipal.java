package com.ai.demo.security;

import com.ai.demo.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
@AllArgsConstructor
public class OAuth2UserPrincipal implements OAuth2User, UserDetails {

    private final User user;

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "googleId", user.getGoogleId() != null ? user.getGoogleId() : "",
                "authProvider", user.getAuthProvider(),
                "profilePicture", user.getProfilePicture() != null ? user.getProfilePicture() : ""
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getEnabled() != null && user.getEnabled();
    }

    @Override
    public String getName() {
        return user.getUsername();
    }
}

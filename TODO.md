# Google OAuth2 Implementation Plan

## Phase 1: Backend - Database & Entity Changes
- [x] 1.1 Update User entity with googleId, authProvider, profilePicture fields
- [x] 1.2 Update UserRepository with findByGoogleId method

## Phase 2: Backend - OAuth2 Configuration
- [ ] 2.1 Update pom.xml - add spring-boot-starter-oauth2-client
- [ ] 2.2 Update application.yml - add Google OAuth2 config
- [ ] 2.3 Update SecurityConfig.java - add oauth2Login()
- [ ] 2.4 Create CustomOAuth2UserService.java
- [ ] 2.5 Create OAuth2AuthenticationSuccessHandler.java

## Phase 3: Frontend - OAuth2 Integration
- [ ] 3.1 Update login.component.ts - add Google button
- [ ] 3.2 Update auth.service.ts - add handleOAuth2Response
- [ ] 3.3 Create oauth-callback.component.ts
- [ ] 3.4 Update app.routes.ts

## Phase 4: Testing
- [ ] 4.1 Get Google credentials from console
- [ ] 4.2 Test the flow

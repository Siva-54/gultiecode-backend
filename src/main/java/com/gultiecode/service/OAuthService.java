package com.gultiecode.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gultiecode.dto.AuthDtos.AuthResponse;
import com.gultiecode.dto.AuthDtos.UserDto;
import com.gultiecode.dto.OAuthRecords.OAuthCallbackRequest;
import com.gultiecode.entity.Role;
import com.gultiecode.entity.User;
import com.gultiecode.repository.UserRepository;
import com.gultiecode.security.JwtTokenProvider;
import com.gultiecode.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final AuthService authService;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private String supabaseServiceRoleKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Main entry point: verifies the Supabase token, upserts the local user,
     * and returns a Gultie JWT so the frontend works exactly as with email/password login.
     */
    @Transactional
    public AuthResponse oauthLogin(OAuthCallbackRequest request) {
        // 1. Verify the Supabase token and extract user metadata
        SupabaseUser supabaseUser = verifySupabaseToken(request.supabaseAccessToken());

        // 2. Upsert local user
        User user = upsertOAuthUser(supabaseUser, request.provider());

        // 3. Update login streak
        updateLoginStreak(user);

        // 4. Build a Spring Security Authentication object (no password needed)
        UserPrincipal principal = UserPrincipal.create(user);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null,
                List.of(new SimpleGrantedAuthority(user.getRole().name()))
        );

        // 5. Generate Gultie JWT
        String gultieToken = tokenProvider.generateToken(auth);

        return AuthResponse.builder()
                .accessToken(gultieToken)
                .tokenType("Bearer")
                .user(authService.mapToUserDto(user))
                .message("OAuth login successful")
                .build();
    }

    /**
     * Calls Supabase /auth/v1/user with the provided access token.
     * Uses the anon key in Authorization header as required by Supabase.
     */
    private SupabaseUser verifySupabaseToken(String accessToken) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(supabaseUrl + "/auth/v1/user"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("apikey", supabaseServiceRoleKey)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Invalid Supabase token. Status: " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());

            String email = root.path("email").asText(null);
            if (email == null || email.isBlank()) {
                throw new RuntimeException("Email not returned by OAuth provider. Please ensure your Google account has a verified email.");
            }

            // Extract name and avatar from user_metadata (provided by Google)
            JsonNode meta = root.path("user_metadata");
            String fullName = meta.path("full_name").asText(null);
            if (fullName == null || fullName.isBlank()) {
                fullName = meta.path("name").asText(email.split("@")[0]);
            }
            String avatarUrl = meta.path("avatar_url").asText(null);
            if (avatarUrl == null || avatarUrl.isBlank()) {
                avatarUrl = meta.path("picture").asText(null);
            }

            String supabaseId = root.path("id").asText(null);

            return new SupabaseUser(supabaseId, email, fullName, avatarUrl);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify Supabase token: " + e.getMessage(), e);
        }
    }

    /**
     * Finds or creates a local User for the OAuth login.
     * OAuth users are auto-approved (Google has verified their email).
     */
    private User upsertOAuthUser(SupabaseUser supabaseUser, String provider) {
        Optional<User> existing = userRepository.findByEmail(supabaseUser.email());
        if (existing.isPresent()) {
            User user = existing.get();
            // Update avatar if not set
            if (user.getAvatarUrl() == null && supabaseUser.avatarUrl() != null) {
                user.setAvatarUrl(supabaseUser.avatarUrl());
                userRepository.save(user);
            }
            return user;
        }

        // Auto-generate a unique username from email prefix
        String baseUsername = supabaseUser.email().split("@")[0].replaceAll("[^a-zA-Z0-9_]", "");
        String username = baseUsername;
        int suffix = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + suffix++;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(supabaseUser.email());
        // OAuth users don't use a password — store an unusable hash
        user.setPassword(UUID.randomUUID().toString());
        user.setFullName(supabaseUser.fullName() != null ? supabaseUser.fullName() : username);
        user.setAvatarUrl(
                supabaseUser.avatarUrl() != null
                        ? supabaseUser.avatarUrl()
                        : "https://api.dicebear.com/7.x/bottts/svg?seed=" + username
        );
        user.setRole(Role.ROLE_USER);
        // Auto-approve OAuth users — Google has already verified their email
        user.setApproved(true);
        user.setApprovalStatus("APPROVED");
        user.setEmailVerified(true);
        user.setTotalPoints(0);
        user.setStreakDays(0);
        user.setLongestStreakDays(0);
        user.setCreatedBy("oauth:" + provider);
        userRepository.save(user);
        return user;
    }

    private void updateLoginStreak(User user) {
        LocalDate today = LocalDate.now();
        LocalDate lastLogin = user.getLastLoginDate();

        if (lastLogin == null) {
            user.setStreakDays(1);
        } else if (lastLogin.equals(today)) {
            // Already logged in today — no change
        } else if (lastLogin.equals(today.minusDays(1))) {
            user.setStreakDays(user.getStreakDays() + 1);
        } else {
            user.setStreakDays(1);
        }

        user.setLastLoginDate(today);
        if (user.getStreakDays() > user.getLongestStreakDays()) {
            user.setLongestStreakDays(user.getStreakDays());
        }
        userRepository.save(user);
    }

    /** Value object holding user data returned by Supabase. */
    private record SupabaseUser(String id, String email, String fullName, String avatarUrl) {}
}

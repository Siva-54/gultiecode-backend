package com.gultiecode.controller;

import com.gultiecode.dto.AuthDtos.AuthResponse;
import com.gultiecode.dto.OAuthRecords.OAuthCallbackRequest;
import com.gultiecode.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;

    /**
     * Called by the frontend after Supabase completes the OAuth redirect.
     * Accepts the Supabase access token, verifies it, upserts the local user,
     * and returns a Gultie JWT so the app can function normally.
     */
    @PostMapping("/callback")
    public ResponseEntity<AuthResponse> oauthCallback(@RequestBody OAuthCallbackRequest request) {
        AuthResponse response = oAuthService.oauthLogin(request);
        return ResponseEntity.ok(response);
    }
}

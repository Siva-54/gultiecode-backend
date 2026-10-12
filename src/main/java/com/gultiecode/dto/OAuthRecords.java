package com.gultiecode.dto;

public class OAuthRecords {

    /**
     * Request body sent by the frontend after Supabase OAuth redirect.
     * The frontend sends the Supabase access token so the backend can verify it.
     */
    public record OAuthCallbackRequest(
            String provider,            // e.g. "google"
            String supabaseAccessToken  // Supabase JWT from supabase.auth.getSession()
    ) {}
}

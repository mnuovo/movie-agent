package com.michelenuovo.movieagent.client.tmdb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TmdbClientAuthModeTest {

    @Test
    void prefersBearerTokenWhenBothCredentialsArePresent() {
        TmdbClient.AuthMode mode = TmdbClient.resolveAuthMode("token", "api-key");

        assertEquals(TmdbClient.AuthMode.BEARER_TOKEN, mode);
    }

    @Test
    void usesApiKeyWhenBearerTokenIsMissing() {
        TmdbClient.AuthMode mode = TmdbClient.resolveAuthMode("", "api-key");

        assertEquals(TmdbClient.AuthMode.API_KEY, mode);
    }

    @Test
    void returnsNoneWhenNoCredentialExists() {
        TmdbClient.AuthMode mode = TmdbClient.resolveAuthMode("", "");

        assertEquals(TmdbClient.AuthMode.NONE, mode);
    }
}



package com.michelenuovo.movieagent.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Strongly typed TMDB configuration properties bound from the {@code tmdb.api} prefix.
 *
 * <p>This record centralizes all integration settings required by {@code TmdbClient}, including
 * the TMDB base URL, supported authentication credentials, and optional TLS trust-store settings
 * for environments that need custom certificate trust.
 *
 * @param baseUrl base TMDB API URL, for example {@code https://api.themoviedb.org/3}
 * @param apiKey optional TMDB API key used as a fallback authentication mechanism
 * @param readAccessToken preferred TMDB read access token used for bearer authentication
 * @param trustStorePath optional path to a custom trust-store file for outbound TLS connections
 * @param trustStorePassword optional password for the configured trust store
 * @param trustStoreType trust-store type such as {@code PKCS12} or {@code JKS}
 */
@ConfigurationProperties(prefix = "tmdb.api")
public record TmdbProperties(
        String baseUrl,
        String apiKey,
        String readAccessToken,
        String trustStorePath,
        String trustStorePassword,
        String trustStoreType
) {
}



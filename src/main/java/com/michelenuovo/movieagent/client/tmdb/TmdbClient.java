package com.michelenuovo.movieagent.client.tmdb;

import com.michelenuovo.movieagent.dto.tmdb.search.movie.TmdbMovieSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.movie.TmdbMovieSearchResponse;
import com.michelenuovo.movieagent.dto.tmdb.search.keyword.TmdbKeywordSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.keyword.TmdbKeywordSearchResponse;
import com.michelenuovo.movieagent.dto.tmdb.search.person.TmdbPersonSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.person.TmdbPersonSearchResponse;
import com.michelenuovo.movieagent.dto.tmdb.search.tv.TmdbTvSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.tv.TmdbTvSearchResponse;
import com.michelenuovo.movieagent.properties.TmdbProperties;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import com.michelenuovo.movieagent.config.CorrelationIdWebFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/**
 * Reactive client for The Movie Database (TMDB) API.
 *
 * <p>This component centralizes outbound calls to TMDB for movie and TV detail lookups and for
 * search operations across movies, TV shows, people, and keywords. It also encapsulates TMDB
 * authentication handling and optional SSL trust store customization.
 *
 * <p>The client exposes reactive {@link Mono} return types so higher layers can remain
 * non-blocking. Synchronous callers, such as tool entry points, may still choose to block on the
 * returned publishers at the application boundary.
 *
 * <p>Authentication is resolved dynamically per request using the configured TMDB credentials. A
 * read access token is preferred when present; otherwise the client falls back to classic API key
 * query-parameter authentication. If neither credential is available, requests fail fast before
 * any outbound network call is attempted.
 *
 * <p>When running behind enterprise proxies or TLS-inspecting gateways, callers can optionally
 * configure a custom trust store so Reactor Netty trusts the required certificate chain.
 */
@Component
public class TmdbClient {

    private static final Logger logger = LoggerFactory.getLogger(TmdbClient.class);

    private static final String API_KEY_QUERY_PARAM = "api_key";
    private static final String SEARCH_PATH_SEGMENT = "search";
    private static final String QUERY_QUERY_PARAM = "query";
    private static final String INCLUDE_ADULT_QUERY_PARAM = "include_adult";
    private static final String LANGUAGE_QUERY_PARAM = "language";
    private static final String PAGE_QUERY_PARAM = "page";
    private static final String MISSING_CREDENTIALS_ERROR = "TMDB credentials are missing. Set TMDB_READ_ACCESS_TOKEN or TMDB_API_KEY.";
    private static final String EMPTY_QUERY_ERROR = "Search query must not be blank.";

    private final WebClient webClient;
    private final TmdbProperties properties;

    /**
     * Creates a TMDB client configured with the application base URL, authentication properties,
     * and optional SSL trust-store support.
     *
     * @param builder Spring-managed {@link WebClient} builder
     * @param properties strongly typed TMDB configuration properties
     */
    public TmdbClient(WebClient.Builder builder, TmdbProperties properties) {
        this.properties = properties;
        this.webClient = builder
                .baseUrl(properties.baseUrl())
                .clientConnector(new ReactorClientHttpConnector(buildHttpClient(properties)))
                .build();
        logger.info("TMDB client initialized with baseUrl={} authMode={}", properties.baseUrl(), resolveAuthMode(properties.readAccessToken(), properties.apiKey()));
    }

    /**
     * Creates the underlying Reactor Netty client.
     *
     * <p>When a trust store path is configured, a custom SSL context is installed; otherwise the
     * default JVM trust configuration is used.
     *
     * @param properties TMDB configuration properties
     * @return configured Reactor Netty HTTP client
     */
    private static HttpClient buildHttpClient(TmdbProperties properties) {
        if (StringUtils.hasText(properties.trustStorePath())) {
            TrustManagerFactory trustManagerFactory = buildTrustManagerFactory(
                    properties.trustStorePath(),
                    properties.trustStorePassword(),
                    properties.trustStoreType());
            SslContext sslContext = buildTrustStoreSslContext(trustManagerFactory);
            return HttpClient.create().secure(ssl -> ssl.sslContext(sslContext));
        }

        return HttpClient.create();
    }

    /**
     * Builds a client SSL context from the provided trust-manager factory.
     *
     * @param trustManagerFactory trust manager factory initialized from the configured trust store
     * @return SSL context ready to be installed in Reactor Netty
     */
    private static SslContext buildTrustStoreSslContext(TrustManagerFactory trustManagerFactory) {
        try {
            return SslContextBuilder.forClient()
                    .trustManager(trustManagerFactory)
                    .build();
        } catch (SSLException exception) {
            throw new IllegalStateException("Failed to initialize TMDB SSL context from truststore.", exception);
        }
    }

    /**
     * Loads the configured trust store and exposes it as a {@link TrustManagerFactory}.
     *
     * <p>This method is package-visible for focused unit testing of trust-store initialization.
     * Invalid paths, passwords, types, or file contents are wrapped in descriptive
     * {@link IllegalStateException}s so configuration issues are easier to diagnose.
     *
     * @param trustStorePath absolute or resolvable path to the trust-store file
     * @param trustStorePassword trust-store password, or {@code null} for an empty password
     * @param trustStoreType store type such as {@code PKCS12} or {@code JKS}
     * @return initialized trust-manager factory for outbound TLS validation
     */
    static TrustManagerFactory buildTrustManagerFactory(String trustStorePath, String trustStorePassword, String trustStoreType) {
        String resolvedType = StringUtils.hasText(trustStoreType) ? trustStoreType : "PKCS12";
        char[] password = trustStorePassword == null ? new char[0] : trustStorePassword.toCharArray();

        try (InputStream inputStream = Files.newInputStream(Path.of(trustStorePath))) {
            KeyStore trustStore = KeyStore.getInstance(resolvedType);
            trustStore.load(inputStream, password);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            return trustManagerFactory;
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to read TMDB truststore at path: " + trustStorePath,
                    exception);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException exception) {
            throw new IllegalStateException(
                    "Invalid TMDB truststore configuration (type/password/content): " + trustStorePath,
                    exception);
        }
    }

    /**
     * Retrieves the raw TMDB movie payload for the supplied movie identifier.
     *
     * @param movieId TMDB movie id
     * @return raw JSON response from TMDB
     */
    public Mono<String> getMovieById(long movieId) {
        logger.debug("Calling TMDB movie detail endpoint: movieId={}", movieId);
        return logTmdbCall("getMovieById", requireAuthMode().flatMap(authMode -> webClient
                .get()
                .uri(uriBuilder -> buildMovieUri(uriBuilder, movieId, authMode))
                .headers(headers -> applyAuthHeaders(headers, authMode))
                .retrieve()
                .bodyToMono(String.class)));
    }

    /**
     * Retrieves the raw TMDB TV payload for the supplied TV identifier.
     *
     * @param tvId TMDB TV id
     * @return raw JSON response from TMDB
     */
    public Mono<String> getTvById(long tvId) {
        logger.debug("Calling TMDB TV detail endpoint: tvId={}", tvId);
        return logTmdbCall("getTvById", requireAuthMode().flatMap(authMode -> webClient
                .get()
                .uri(uriBuilder -> buildTvUri(uriBuilder, tvId, authMode))
                .headers(headers -> applyAuthHeaders(headers, authMode))
                .retrieve()
                .bodyToMono(String.class)));
    }

    /**
     * Searches TMDB movies using the provided query and optional movie filters.
     *
     * <p>The request is validated locally before the outbound call is attempted.
     *
     * @param request movie search request; must contain a non-blank {@code query}
     * @return TMDB movie search response
     */
    public Mono<TmdbMovieSearchResponse> searchMovies(TmdbMovieSearchRequest request) {
        if (request == null || !StringUtils.hasText(request.query())) {
            logger.warn("Rejected TMDB movie search request due to blank query.");
            return Mono.error(new IllegalArgumentException(EMPTY_QUERY_ERROR));
        }

        logger.debug("Calling TMDB movie search: queryLength={} page={}", request.query().length(), request.page());
        return logTmdbCall("searchMovies", requireAuthMode().flatMap(authMode -> webClient
                .get()
                .uri(uriBuilder -> buildSearchMovieUri(uriBuilder, request, authMode))
                .headers(headers -> applyAuthHeaders(headers, authMode))
                .retrieve()
                .bodyToMono(TmdbMovieSearchResponse.class)));
    }

    /**
     * Searches TMDB TV shows using the provided query and optional TV filters.
     *
     * <p>The request is validated locally before the outbound call is attempted.
     *
     * @param request TV search request; must contain a non-blank {@code query}
     * @return TMDB TV search response
     */
    public Mono<TmdbTvSearchResponse> searchTv(TmdbTvSearchRequest request) {
        if (request == null || !StringUtils.hasText(request.query())) {
            logger.warn("Rejected TMDB TV search request due to blank query.");
            return Mono.error(new IllegalArgumentException(EMPTY_QUERY_ERROR));
        }

        logger.debug("Calling TMDB TV search: queryLength={} page={}", request.query().length(), request.page());
        return logTmdbCall("searchTv", requireAuthMode().flatMap(authMode -> webClient
                .get()
                .uri(uriBuilder -> buildSearchTvUri(uriBuilder, request, authMode))
                .headers(headers -> applyAuthHeaders(headers, authMode))
                .retrieve()
                .bodyToMono(TmdbTvSearchResponse.class)));
    }

    /**
     * Searches TMDB people by name.
     *
     * <p>The request is validated locally before the outbound call is attempted.
     *
     * @param request person search request; must contain a non-blank {@code query}
     * @return TMDB person search response
     */
    public Mono<TmdbPersonSearchResponse> searchPerson(TmdbPersonSearchRequest request) {
        if (request == null || !StringUtils.hasText(request.query())) {
            logger.warn("Rejected TMDB person search request due to blank query.");
            return Mono.error(new IllegalArgumentException(EMPTY_QUERY_ERROR));
        }

        logger.debug("Calling TMDB person search: queryLength={} page={}", request.query().length(), request.page());
        return logTmdbCall("searchPerson", requireAuthMode().flatMap(authMode -> webClient
                .get()
                .uri(uriBuilder -> buildSearchPersonUri(uriBuilder, request, authMode))
                .headers(headers -> applyAuthHeaders(headers, authMode))
                .retrieve()
                .bodyToMono(TmdbPersonSearchResponse.class)));
    }

    /**
     * Searches TMDB keywords by name.
     *
     * <p>The request is validated locally before the outbound call is attempted.
     *
     * @param request keyword search request; must contain a non-blank {@code query}
     * @return TMDB keyword search response
     */
    public Mono<TmdbKeywordSearchResponse> searchKeyword(TmdbKeywordSearchRequest request) {
        if (request == null || !StringUtils.hasText(request.query())) {
            logger.warn("Rejected TMDB keyword search request due to blank query.");
            return Mono.error(new IllegalArgumentException(EMPTY_QUERY_ERROR));
        }

        logger.debug("Calling TMDB keyword search: queryLength={} page={}", request.query().length(), request.page());
        return logTmdbCall("searchKeyword", requireAuthMode().flatMap(authMode -> webClient
                .get()
                .uri(uriBuilder -> buildSearchKeywordUri(uriBuilder, request, authMode))
                .headers(headers -> applyAuthHeaders(headers, authMode))
                .retrieve()
                .bodyToMono(TmdbKeywordSearchResponse.class)));
    }

    /**
     * Resolves the active authentication mode and fails fast when no supported TMDB credentials
     * are configured.
     *
     * @return a mono emitting the resolved authentication mode
     */
    private Mono<AuthMode> requireAuthMode() {
        AuthMode authMode = resolveAuthMode(properties.readAccessToken(), properties.apiKey());
        if (authMode == AuthMode.NONE) {
            logger.error("TMDB auth mode resolution failed due to missing credentials.");
            return Mono.error(new IllegalStateException(MISSING_CREDENTIALS_ERROR));
        }
        return Mono.just(authMode);
    }

    private <T> Mono<T> logTmdbCall(String operation, Mono<T> upstreamCall) {
        return upstreamCall
                .doOnEach(CorrelationIdWebFilter.withCorrelationMdc(signal -> {
                    if (signal.isOnNext()) {
                        logger.debug("TMDB operation completed: {}", operation);
                    } else if (signal.isOnError()) {
                        Throwable throwable = signal.getThrowable();
                        if (throwable instanceof WebClientResponseException ex) {
                            logger.warn("TMDB operation failed: {} status={} message={}", operation, ex.getStatusCode(), ex.getMessage());
                        } else {
                            logger.error("TMDB operation failed: {}", operation, throwable);
                        }
                    }
                }));
    }

    /**
     * Builds the URI for {@code GET /movie/{movieId}}.
     *
     * <p>When API key authentication is active, the key is appended as a query parameter.
     *
     * @param uriBuilder Spring URI builder rooted at the configured TMDB base URL
     * @param movieId TMDB movie identifier
     * @param authMode resolved authentication mode
     * @return fully constructed request URI
     */
    private URI buildMovieUri(UriBuilder uriBuilder, long movieId, AuthMode authMode) {
        uriBuilder.pathSegment("movie", String.valueOf(movieId));
        if (authMode == AuthMode.API_KEY) {
            uriBuilder.queryParam(API_KEY_QUERY_PARAM, properties.apiKey());
        }
        return uriBuilder.build();
    }

    /**
     * Builds the URI for {@code GET /tv/{tvId}}.
     *
     * <p>When API key authentication is active, the key is appended as a query parameter.
     *
     * @param uriBuilder Spring URI builder rooted at the configured TMDB base URL
     * @param tvId TMDB TV identifier
     * @param authMode resolved authentication mode
     * @return fully constructed request URI
     */
    private URI buildTvUri(UriBuilder uriBuilder, long tvId, AuthMode authMode) {
        uriBuilder.pathSegment("tv", String.valueOf(tvId));
        if (authMode == AuthMode.API_KEY) {
            uriBuilder.queryParam(API_KEY_QUERY_PARAM, properties.apiKey());
        }
        return uriBuilder.build();
    }

    /**
     * Builds the URI for {@code GET /search/movie}.
     *
     * <p>Always sends {@code query}, {@code include_adult}, {@code language}, and {@code page}.
     * Optional TMDB movie filters such as {@code primary_release_year}, {@code region}, and
     * {@code year} are included only when present on the request.
     *
     * @param uriBuilder Spring URI builder rooted at the configured TMDB base URL
     * @param request movie-search request
     * @param authMode resolved authentication mode
     * @return fully constructed request URI
     */
    private URI buildSearchMovieUri(UriBuilder uriBuilder, TmdbMovieSearchRequest request, AuthMode authMode) {
        uriBuilder.pathSegment(SEARCH_PATH_SEGMENT, "movie")
                .queryParam(QUERY_QUERY_PARAM, request.query())
                .queryParam(INCLUDE_ADULT_QUERY_PARAM, request.includeAdult())
                .queryParam(LANGUAGE_QUERY_PARAM, request.language())
                .queryParam(PAGE_QUERY_PARAM, request.page());

        if (StringUtils.hasText(request.primaryReleaseYear())) {
            uriBuilder.queryParam("primary_release_year", request.primaryReleaseYear());
        }
        if (StringUtils.hasText(request.region())) {
            uriBuilder.queryParam("region", request.region());
        }
        if (StringUtils.hasText(request.year())) {
            uriBuilder.queryParam("year", request.year());
        }
        if (authMode == AuthMode.API_KEY) {
            uriBuilder.queryParam(API_KEY_QUERY_PARAM, properties.apiKey());
        }
        return uriBuilder.build();
    }

    /**
     * Builds the URI for {@code GET /search/tv}.
     *
     * <p>Always sends {@code query}, {@code include_adult}, {@code language}, and {@code page}.
     * Optional TMDB TV filters such as {@code first_air_date_year} and {@code year} are included
     * only when present on the request.
     *
     * @param uriBuilder Spring URI builder rooted at the configured TMDB base URL
     * @param request TV-search request
     * @param authMode resolved authentication mode
     * @return fully constructed request URI
     */
    private URI buildSearchTvUri(UriBuilder uriBuilder, TmdbTvSearchRequest request, AuthMode authMode) {
        uriBuilder.pathSegment(SEARCH_PATH_SEGMENT, "tv")
                .queryParam(QUERY_QUERY_PARAM, request.query())
                .queryParam(INCLUDE_ADULT_QUERY_PARAM, request.includeAdult())
                .queryParam(LANGUAGE_QUERY_PARAM, request.language())
                .queryParam(PAGE_QUERY_PARAM, request.page());

        if (request.firstAirDateYear() != null) {
            uriBuilder.queryParam("first_air_date_year", request.firstAirDateYear());
        }
        if (request.year() != null) {
            uriBuilder.queryParam("year", request.year());
        }
        if (authMode == AuthMode.API_KEY) {
            uriBuilder.queryParam(API_KEY_QUERY_PARAM, properties.apiKey());
        }
        return uriBuilder.build();
    }

    /**
     * Builds the URI for {@code GET /search/person}.
     *
     * <p>The TMDB person search supports {@code query}, {@code include_adult},
     * {@code language}, and {@code page} in this client.
     *
     * @param uriBuilder Spring URI builder rooted at the configured TMDB base URL
     * @param request person-search request
     * @param authMode resolved authentication mode
     * @return fully constructed request URI
     */
    private URI buildSearchPersonUri(UriBuilder uriBuilder, TmdbPersonSearchRequest request, AuthMode authMode) {
        uriBuilder.pathSegment(SEARCH_PATH_SEGMENT, "person")
                .queryParam(QUERY_QUERY_PARAM, request.query())
                .queryParam(INCLUDE_ADULT_QUERY_PARAM, request.includeAdult())
                .queryParam(LANGUAGE_QUERY_PARAM, request.language())
                .queryParam(PAGE_QUERY_PARAM, request.page());

        if (authMode == AuthMode.API_KEY) {
            uriBuilder.queryParam(API_KEY_QUERY_PARAM, properties.apiKey());
        }
        return uriBuilder.build();
    }

    /**
     * Builds the URI for {@code GET /search/keyword}.
     *
     * <p>The TMDB keyword search supports {@code query} and {@code page} in this client.
     *
     * @param uriBuilder Spring URI builder rooted at the configured TMDB base URL
     * @param request keyword-search request
     * @param authMode resolved authentication mode
     * @return fully constructed request URI
     */
    private URI buildSearchKeywordUri(UriBuilder uriBuilder, TmdbKeywordSearchRequest request, AuthMode authMode) {
        uriBuilder.pathSegment(SEARCH_PATH_SEGMENT, "keyword")
                .queryParam(QUERY_QUERY_PARAM, request.query())
                .queryParam(PAGE_QUERY_PARAM, request.page());

        if (authMode == AuthMode.API_KEY) {
            uriBuilder.queryParam(API_KEY_QUERY_PARAM, properties.apiKey());
        }
        return uriBuilder.build();
    }

    /**
     * Applies bearer authentication when the resolved mode uses a TMDB read access token.
     *
     * <p>API key authentication is handled at URI construction time through query parameters.
     *
     * @param headers outbound request headers to mutate
     * @param authMode resolved authentication mode
     */
    private void applyAuthHeaders(HttpHeaders headers, AuthMode authMode) {
        if (authMode == AuthMode.BEARER_TOKEN) {
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + properties.readAccessToken());
        }
    }

    /**
     * Determines which TMDB authentication mechanism should be used.
     *
     * <p>When both a bearer token and API key are present, bearer authentication takes
     * precedence.
     *
     * @param readAccessToken configured TMDB read access token
     * @param apiKey configured TMDB API key
     * @return resolved authentication mode
     */
    static AuthMode resolveAuthMode(String readAccessToken, String apiKey) {
        if (StringUtils.hasText(readAccessToken)) {
            return AuthMode.BEARER_TOKEN;
        }
        if (StringUtils.hasText(apiKey)) {
            return AuthMode.API_KEY;
        }
        return AuthMode.NONE;
    }

    /**
     * Supported TMDB authentication modes.
     *
     * <p>{@link #BEARER_TOKEN} uses the HTTP {@code Authorization} header,
     * {@link #API_KEY} appends the credential as a query parameter, and {@link #NONE} represents
     * a misconfigured state where no TMDB credential is available.
     */
    enum AuthMode {
        BEARER_TOKEN,
        API_KEY,
        NONE
    }
}

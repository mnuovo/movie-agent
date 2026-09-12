package com.michelenuovo.movieagent.controller;

import com.michelenuovo.movieagent.client.tmdb.TmdbClient;
import com.michelenuovo.movieagent.config.CorrelationIdWebFilter;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

/**
 * REST controller exposing direct pass-through TMDB lookup endpoints.
 *
 * <p>These endpoints are useful for manual verification and lightweight API access when a caller
 * needs raw TMDB detail payloads for movies or TV shows without going through the LLM-backed chat
 * flow.
 */
@RestController
@RequestMapping("/tmdb")
class TmdbController {

    private static final Logger logger = LoggerFactory.getLogger(TmdbController.class);

    private final TmdbClient tmdbClient;

    TmdbController(TmdbClient tmdbClient) {
        this.tmdbClient = tmdbClient;
    }

    /**
     * Fetches a movie by TMDB id and proxies the raw response body.
     *
     * @param movieId TMDB movie identifier
     * @return reactive HTTP response containing TMDB movie details
     */
    @GetMapping("/movie/{movieId}")
    Mono<ResponseEntity<String>> movieById(@PathVariable long movieId) {
        logger.info("Received TMDB movie detail request: movieId={}", movieId);
        return tmdbClient.getMovieById(movieId)
                .map(ResponseEntity::ok)
                .doOnEach(CorrelationIdWebFilter.withCorrelationMdc(detailSignalLogger("movie", movieId)));
    }

    /**
     * Fetches a TV show by TMDB id and proxies the raw response body.
     *
     * @param tvId TMDB TV identifier
     * @return reactive HTTP response containing TMDB TV details
     */
    @GetMapping("/tv/{tvId}")
    Mono<ResponseEntity<String>> tvById(@PathVariable long tvId) {
        logger.info("Received TMDB TV detail request: tvId={}", tvId);
        return tmdbClient.getTvById(tvId)
                .map(ResponseEntity::ok)
                .doOnEach(CorrelationIdWebFilter.withCorrelationMdc(detailSignalLogger("tv", tvId)));
    }

    private Consumer<Signal<ResponseEntity<String>>> detailSignalLogger(String mediaType, long id) {
        return signal -> logDetailSignal(mediaType, id, signal);
    }

    private void logDetailSignal(String mediaType, long id, Signal<ResponseEntity<String>> signal) {
        if (signal.isOnNext()) {
            logger.debug("TMDB {} detail request completed: id={}", mediaType, id);
            return;
        }
        if (signal.isOnError()) {
            logger.error("TMDB {} detail request failed: id={}", mediaType, id, signal.getThrowable());
        }
    }
}

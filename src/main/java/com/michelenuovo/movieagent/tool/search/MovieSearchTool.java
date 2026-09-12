package com.michelenuovo.movieagent.tool.search;

import com.michelenuovo.movieagent.client.tmdb.TmdbClient;
import com.michelenuovo.movieagent.dto.tmdb.search.movie.TmdbMovieSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.movie.TmdbMovieSearchResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Specialized TMDB movie search tool.
 *
 * <p>Use this when an integration prefers a movie-only contract instead of the unified
 * {@link MediaSearchTool}. It is optional when only the unified tool is registered.
 */
@Component
public class MovieSearchTool {

    private final TmdbClient tmdbClient;

    public MovieSearchTool(TmdbClient tmdbClient) {
        this.tmdbClient = tmdbClient;
    }

    /**
     * Calls TMDB movie search directly and returns the movie-specific response payload.
     */
    @Tool(name = "movieSearchTool", description = "Search movies in TMDB by title and optional filters")
    public Mono<TmdbMovieSearchResponse> movieSearchTool(TmdbMovieSearchRequest request) {
        return tmdbClient.searchMovies(request);
    }
}


package com.michelenuovo.movieagent.tool.strategy;

import com.michelenuovo.movieagent.client.tmdb.TmdbClient;
import com.michelenuovo.movieagent.dto.tmdb.search.media.SearchMediaType;
import com.michelenuovo.movieagent.dto.tmdb.search.media.TmdbMediaSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.media.TmdbMediaSearchResponse;
import com.michelenuovo.movieagent.tool.mapper.MediaSearchMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Strategy implementation for {@link SearchMediaType#MOVIE} searches.
 *
 * <p>This strategy delegates movie-specific request construction to {@link MediaSearchMapper},
 * executes the TMDB movie search through {@link TmdbClient}, and converts the result back into the
 * unified media response model expected by the tool layer.
 */
@Component
public class MovieMediaSearchStrategy implements MediaSearchStrategy {

    private static final String RESPONSE_ERROR = "TMDB movie search returned no response.";

    private final TmdbClient tmdbClient;
    private final MediaSearchMapper mediaSearchMapper;

    public MovieMediaSearchStrategy(TmdbClient tmdbClient, MediaSearchMapper mediaSearchMapper) {
        this.tmdbClient = tmdbClient;
        this.mediaSearchMapper = mediaSearchMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchMediaType mediaType() {
        return SearchMediaType.MOVIE;
    }

    /**
     * Executes a movie-only TMDB search and normalizes the response.
     *
     * @param request unified media search request
     * @return publisher of normalized movie search results
     */
    @Override
    public Mono<TmdbMediaSearchResponse> search(TmdbMediaSearchRequest request) {
        return tmdbClient.searchMovies(mediaSearchMapper.toMovieRequest(request))
                .switchIfEmpty(Mono.error(new IllegalStateException(RESPONSE_ERROR)))
                .map(mediaSearchMapper::mapMovieResponse);
    }
}



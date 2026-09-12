package com.michelenuovo.movieagent.tool.strategy;

import com.michelenuovo.movieagent.client.tmdb.TmdbClient;
import com.michelenuovo.movieagent.dto.tmdb.search.keyword.TmdbKeywordSearchResponse;
import com.michelenuovo.movieagent.dto.tmdb.search.media.SearchMediaType;
import com.michelenuovo.movieagent.dto.tmdb.search.media.TmdbMediaSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.media.TmdbMediaSearchResponse;
import com.michelenuovo.movieagent.dto.tmdb.search.movie.TmdbMovieSearchResponse;
import com.michelenuovo.movieagent.dto.tmdb.search.person.TmdbPersonSearchResponse;
import com.michelenuovo.movieagent.dto.tmdb.search.tv.TmdbTvSearchResponse;
import com.michelenuovo.movieagent.tool.mapper.MediaSearchMapper;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Strategy implementation for {@link SearchMediaType#AUTO} searches.
 *
 * <p>This strategy performs a broader discovery flow by executing movie, TV, person, and keyword
 * searches against TMDB, normalizing each response through {@link MediaSearchMapper}, and merging
 * the results into a single aggregated response.
 */
@Component
public class AutoMediaSearchStrategy implements MediaSearchStrategy {

    private static final String MOVIE_RESPONSE_ERROR = "TMDB movie search returned no response.";
    private static final String TV_RESPONSE_ERROR = "TMDB TV search returned no response.";
    private static final String PERSON_RESPONSE_ERROR = "TMDB person search returned no response.";
    private static final String KEYWORD_RESPONSE_ERROR = "TMDB keyword search returned no response.";

    private final TmdbClient tmdbClient;
    private final MediaSearchMapper mediaSearchMapper;

    public AutoMediaSearchStrategy(TmdbClient tmdbClient, MediaSearchMapper mediaSearchMapper) {
        this.tmdbClient = tmdbClient;
        this.mediaSearchMapper = mediaSearchMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchMediaType mediaType() {
        return SearchMediaType.AUTO;
    }

    /**
     * Executes an aggregated cross-media search.
     *
     * <p>Each underlying TMDB call is composed reactively and combined once all source searches
     * complete. Missing client responses fail fast with explicit error messages to simplify
     * debugging while keeping the full flow non-blocking.
     *
     * @param request unified media search request
     * @return merged normalized response spanning multiple media categories
     */
    @Override
    public Mono<TmdbMediaSearchResponse> search(TmdbMediaSearchRequest request) {
        Mono<TmdbMovieSearchResponse> movieResponse = requireResponse(
                tmdbClient.searchMovies(mediaSearchMapper.toMovieRequest(request)),
                MOVIE_RESPONSE_ERROR,
                Function.identity());
        Mono<TmdbTvSearchResponse> tvResponse = requireResponse(
                tmdbClient.searchTv(mediaSearchMapper.toTvRequest(request)),
                TV_RESPONSE_ERROR,
                Function.identity());
        Mono<TmdbPersonSearchResponse> personResponse = requireResponse(
                tmdbClient.searchPerson(mediaSearchMapper.toPersonRequest(request)),
                PERSON_RESPONSE_ERROR,
                Function.identity());
        Mono<TmdbKeywordSearchResponse> keywordResponse = requireResponse(
                tmdbClient.searchKeyword(mediaSearchMapper.toKeywordRequest(request)),
                KEYWORD_RESPONSE_ERROR,
                Function.identity());

        return Mono.zip(movieResponse, tvResponse, personResponse, keywordResponse)
                .map(tuple -> mediaSearchMapper.mergeAutoResponse(List.of(
                        mediaSearchMapper.mapMovieResponse(tuple.getT1()),
                        mediaSearchMapper.mapTvResponse(tuple.getT2()),
                        mediaSearchMapper.mapPersonResponse(tuple.getT3()),
                        mediaSearchMapper.mapKeywordResponse(tuple.getT4()))));
    }

    private <T, R> Mono<R> requireResponse(Mono<T> response, String errorMessage, Function<T, R> mapper) {
        return response
                .switchIfEmpty(Mono.error(new IllegalStateException(errorMessage)))
                .map(mapper);
    }
}

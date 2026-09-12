package com.michelenuovo.movieagent.tool.strategy;

import com.michelenuovo.movieagent.client.tmdb.TmdbClient;
import com.michelenuovo.movieagent.dto.tmdb.search.media.SearchMediaType;
import com.michelenuovo.movieagent.dto.tmdb.search.media.TmdbMediaSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.media.TmdbMediaSearchResponse;
import com.michelenuovo.movieagent.tool.mapper.MediaSearchMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Strategy implementation for {@link SearchMediaType#KEYWORD} searches.
 *
 * <p>This strategy supports TMDB keyword lookup by transforming the generic media request into the
 * narrower keyword request shape and mapping the resulting TMDB payload into the unified media
 * response contract.
 */
@Component
public class KeywordMediaSearchStrategy implements MediaSearchStrategy {

    private static final String RESPONSE_ERROR = "TMDB keyword search returned no response.";

    private final TmdbClient tmdbClient;
    private final MediaSearchMapper mediaSearchMapper;

    public KeywordMediaSearchStrategy(TmdbClient tmdbClient, MediaSearchMapper mediaSearchMapper) {
        this.tmdbClient = tmdbClient;
        this.mediaSearchMapper = mediaSearchMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchMediaType mediaType() {
        return SearchMediaType.KEYWORD;
    }

    /**
     * Executes a keyword-only TMDB search and normalizes the response.
     *
     * @param request unified media search request
     * @return publisher of normalized keyword search results
     */
    @Override
    public Mono<TmdbMediaSearchResponse> search(TmdbMediaSearchRequest request) {
        return tmdbClient.searchKeyword(mediaSearchMapper.toKeywordRequest(request))
                .switchIfEmpty(Mono.error(new IllegalStateException(RESPONSE_ERROR)))
                .map(mediaSearchMapper::mapKeywordResponse);
    }
}



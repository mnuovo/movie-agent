package com.michelenuovo.movieagent.tool.strategy;

import com.michelenuovo.movieagent.client.tmdb.TmdbClient;
import com.michelenuovo.movieagent.dto.tmdb.search.media.SearchMediaType;
import com.michelenuovo.movieagent.dto.tmdb.search.media.TmdbMediaSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.media.TmdbMediaSearchResponse;
import com.michelenuovo.movieagent.tool.mapper.MediaSearchMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Strategy implementation for {@link SearchMediaType#TV} searches.
 *
 * <p>This strategy adapts the generic media-search request into a TV-specific TMDB request,
 * invokes the reactive TMDB client, and maps the TV response into the normalized media result
 * structure consumed by the rest of the application.
 */
@Component
public class TvMediaSearchStrategy implements MediaSearchStrategy {

    private static final String RESPONSE_ERROR = "TMDB TV search returned no response.";

    private final TmdbClient tmdbClient;
    private final MediaSearchMapper mediaSearchMapper;

    public TvMediaSearchStrategy(TmdbClient tmdbClient, MediaSearchMapper mediaSearchMapper) {
        this.tmdbClient = tmdbClient;
        this.mediaSearchMapper = mediaSearchMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchMediaType mediaType() {
        return SearchMediaType.TV;
    }

    /**
     * Executes a TV-only TMDB search and normalizes the response.
     *
     * @param request unified media search request
     * @return publisher of normalized TV search results
     */
    @Override
    public Mono<TmdbMediaSearchResponse> search(TmdbMediaSearchRequest request) {
        return tmdbClient.searchTv(mediaSearchMapper.toTvRequest(request))
                .switchIfEmpty(Mono.error(new IllegalStateException(RESPONSE_ERROR)))
                .map(mediaSearchMapper::mapTvResponse);
    }
}



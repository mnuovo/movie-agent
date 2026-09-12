package com.michelenuovo.movieagent.tool.strategy;

import com.michelenuovo.movieagent.client.tmdb.TmdbClient;
import com.michelenuovo.movieagent.dto.tmdb.search.media.SearchMediaType;
import com.michelenuovo.movieagent.dto.tmdb.search.media.TmdbMediaSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.media.TmdbMediaSearchResponse;
import com.michelenuovo.movieagent.tool.mapper.MediaSearchMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Strategy implementation for {@link SearchMediaType#PERSON} searches.
 *
 * <p>This strategy handles people discovery by converting the unified request into a TMDB
 * person-search request and then normalizing the response into the shared media result model.
 */
@Component
public class PersonMediaSearchStrategy implements MediaSearchStrategy {

    private static final String RESPONSE_ERROR = "TMDB person search returned no response.";

    private final TmdbClient tmdbClient;
    private final MediaSearchMapper mediaSearchMapper;

    public PersonMediaSearchStrategy(TmdbClient tmdbClient, MediaSearchMapper mediaSearchMapper) {
        this.tmdbClient = tmdbClient;
        this.mediaSearchMapper = mediaSearchMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchMediaType mediaType() {
        return SearchMediaType.PERSON;
    }

    /**
     * Executes a person-only TMDB search and normalizes the response.
     *
     * @param request unified media search request
     * @return publisher of normalized person search results
     */
    @Override
    public Mono<TmdbMediaSearchResponse> search(TmdbMediaSearchRequest request) {
        return tmdbClient.searchPerson(mediaSearchMapper.toPersonRequest(request))
                .switchIfEmpty(Mono.error(new IllegalStateException(RESPONSE_ERROR)))
                .map(mediaSearchMapper::mapPersonResponse);
    }
}



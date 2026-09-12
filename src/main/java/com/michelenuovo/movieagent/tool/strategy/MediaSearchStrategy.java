package com.michelenuovo.movieagent.tool.strategy;

import com.michelenuovo.movieagent.dto.tmdb.search.media.SearchMediaType;
import com.michelenuovo.movieagent.dto.tmdb.search.media.TmdbMediaSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.media.TmdbMediaSearchResponse;
import reactor.core.publisher.Mono;

/**
 * Strategy contract for executing a unified media search for a specific {@link SearchMediaType}.
 *
 * <p>Implementations adapt the generic {@link TmdbMediaSearchRequest} used by the tool layer into
 * concrete TMDB client calls and return a normalized {@link TmdbMediaSearchResponse}. This keeps
 * media-type-specific search logic isolated while allowing the tool entry point to route requests
 * through a common abstraction.
 */
public interface MediaSearchStrategy {

    /**
     * Declares which media type this strategy supports.
     *
     * @return the media type handled by this implementation
     */
    SearchMediaType mediaType();

    /**
     * Executes a search for the given unified request and returns normalized results.
     *
     * @param request generic media search request containing shared filters and optional
     * media-specific fields
     * @return publisher of normalized media search response suitable for downstream tool consumers
     */
    Mono<TmdbMediaSearchResponse> search(TmdbMediaSearchRequest request);
}



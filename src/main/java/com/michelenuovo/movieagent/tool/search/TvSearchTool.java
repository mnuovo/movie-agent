package com.michelenuovo.movieagent.tool.search;

import com.michelenuovo.movieagent.client.tmdb.TmdbClient;
import com.michelenuovo.movieagent.dto.tmdb.search.tv.TmdbTvSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.tv.TmdbTvSearchResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Specialized TMDB TV search tool.
 *
 * <p>Use this when an integration prefers a TV-only contract instead of the unified
 * {@link MediaSearchTool}. It is optional when only the unified tool is registered.
 */
@Component
public class TvSearchTool {

    private final TmdbClient tmdbClient;

    public TvSearchTool(TmdbClient tmdbClient) {
        this.tmdbClient = tmdbClient;
    }

    /**
     * Calls TMDB TV search directly and returns the TV-specific response payload.
     */
    @Tool(name = "tvSearchTool", description = "Search TV shows in TMDB by title and optional filters")
    public Mono<TmdbTvSearchResponse> tvSearchTool(TmdbTvSearchRequest request) {
        return tmdbClient.searchTv(request);
    }
}


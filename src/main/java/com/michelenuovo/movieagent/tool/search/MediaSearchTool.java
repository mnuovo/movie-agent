package com.michelenuovo.movieagent.tool.search;

import com.michelenuovo.movieagent.dto.tmdb.search.media.SearchMediaType;
import com.michelenuovo.movieagent.dto.tmdb.search.media.TmdbMediaSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.media.TmdbMediaSearchResponse;
import com.michelenuovo.movieagent.tool.strategy.MediaSearchStrategy;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

/**
 * Unified TMDB search tool used by the chat flow.
 *
 * <p>This tool orchestrates search execution by delegating to a {@link MediaSearchStrategy}
 * based on {@code request.mediaType()} (movie, TV, person, keyword, auto).
 */
@Component
public class MediaSearchTool {

    private final Map<SearchMediaType, MediaSearchStrategy> strategies;

    public MediaSearchTool(List<MediaSearchStrategy> strategies) {
        this.strategies = indexStrategies(strategies);
    }

    /**
     * Entry point exposed to the LLM for media discovery across multiple media types.
     *
     * <p>Routing is explicit: the caller provides {@code mediaType}. In {@code auto} mode, the
     * corresponding strategy aggregates results from multiple TMDB search endpoints.
     */
    @Tool(name = "mediaSearchTool", description = "Search TMDB media by type: movie, tv, person, keyword, or auto")
    public Mono<TmdbMediaSearchResponse> mediaSearchTool(TmdbMediaSearchRequest request) {
        if (request == null || !StringUtils.hasText(request.query())) {
            return Mono.error(new IllegalArgumentException("Search query must not be blank."));
        }

        SearchMediaType mediaType = request.mediaType() == null ? SearchMediaType.AUTO : request.mediaType();
        MediaSearchStrategy strategy = strategies.get(mediaType);
        if (strategy == null) {
            return Mono.error(new IllegalArgumentException("mediaType must be one of: movie, tv, person, keyword, auto."));
        }

        return strategy.search(request);
    }

    private Map<SearchMediaType, MediaSearchStrategy> indexStrategies(List<MediaSearchStrategy> strategies) {
        EnumMap<SearchMediaType, MediaSearchStrategy> registry = new EnumMap<>(SearchMediaType.class);
        for (MediaSearchStrategy strategy : strategies) {
            MediaSearchStrategy previous = registry.put(strategy.mediaType(), strategy);
            if (previous != null) {
                throw new IllegalStateException(
                        "Duplicate media search strategy registered for " + strategy.mediaType());
            }
        }
        return Map.copyOf(registry);
    }
}


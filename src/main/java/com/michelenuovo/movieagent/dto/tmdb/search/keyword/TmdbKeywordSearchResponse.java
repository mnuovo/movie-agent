package com.michelenuovo.movieagent.dto.tmdb.search.keyword;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response DTO returned by the TMDB keyword search endpoint.
 *
 * @param page current result page
 * @param results keyword results for the current page
 * @param totalPages total number of pages available upstream
 * @param totalResults total number of matching keywords upstream
 */
public record TmdbKeywordSearchResponse(
        int page,
        List<KeywordResult> results,
        @JsonProperty("total_pages") int totalPages,
        @JsonProperty("total_results") int totalResults
) {
    /**
     * Keyword result returned by TMDB.
     *
     * @param id TMDB keyword identifier
     * @param name keyword display name
     */
    public record KeywordResult(
            int id,
            String name
    ) {
    }
}


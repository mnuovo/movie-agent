package com.michelenuovo.movieagent.dto.tmdb.search.keyword;

/**
 * Request DTO for searching TMDB keywords.
 *
 * @param query keyword text to search for
 * @param page result page number; defaults to {@code 1}
 */
public record TmdbKeywordSearchRequest(
        String query,
        Integer page
) {
    /**
     * Applies defaults for optional search parameters.
     */
    public TmdbKeywordSearchRequest {
        page = page != null ? page : 1;
    }
}


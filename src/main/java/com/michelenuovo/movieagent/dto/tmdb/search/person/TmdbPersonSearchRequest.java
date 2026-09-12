package com.michelenuovo.movieagent.dto.tmdb.search.person;

/**
 * Request DTO for searching people in TMDB.
 *
 * @param query person name or search term
 * @param includeAdult whether adult-related results may be included; defaults to {@code false}
 * @param language TMDB language code; defaults to {@code en-US}
 * @param page result page number; defaults to {@code 1}
 */
public record TmdbPersonSearchRequest(
        String query,
        Boolean includeAdult,
        String language,
        Integer page
) {
    /**
     * Applies defaults for optional search parameters.
     */
    public TmdbPersonSearchRequest {
        includeAdult = includeAdult != null ? includeAdult : Boolean.FALSE;
        language = (language == null || language.isBlank()) ? "en-US" : language;
        page = page != null ? page : 1;
    }
}


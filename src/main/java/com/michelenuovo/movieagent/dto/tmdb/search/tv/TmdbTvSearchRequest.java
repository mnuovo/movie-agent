package com.michelenuovo.movieagent.dto.tmdb.search.tv;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for searching TV shows in TMDB.
 *
 * @param query TV show title or free-text query
 * @param includeAdult whether adult-related results may be included; defaults to {@code false}
 * @param language TMDB language code; defaults to {@code en-US}
 * @param page result page number; defaults to {@code 1}
 * @param firstAirDateYear optional filter for the show's first-air year
 * @param year optional year filter supported by TMDB
 */
public record TmdbTvSearchRequest(
        String query,
        @JsonProperty("include_adult")
        Boolean includeAdult,
        String language,
        Integer page,
        @JsonProperty("first_air_date_year")
        Integer firstAirDateYear,
        Integer year
) {
    /**
     * Applies defaults for optional search parameters.
     */
    public TmdbTvSearchRequest {
        includeAdult = includeAdult != null ? includeAdult : Boolean.FALSE;
        language = (language == null || language.isBlank()) ? "en-US" : language;
        page = page != null ? page : 1;
    }
}


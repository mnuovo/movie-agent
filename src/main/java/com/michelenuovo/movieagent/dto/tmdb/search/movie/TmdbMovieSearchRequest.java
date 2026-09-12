package com.michelenuovo.movieagent.dto.tmdb.search.movie;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for searching movies in TMDB.
 *
 * @param query movie title or free-text query
 * @param includeAdult whether adult movie results may be included; defaults to {@code false}
 * @param language TMDB language code; defaults to {@code en-US}
 * @param primaryReleaseYear optional movie filter for primary release year
 * @param page result page number; defaults to {@code 1}
 * @param region optional TMDB region filter
 * @param year optional release year filter
 */
public record TmdbMovieSearchRequest(
        String query,
        @JsonProperty("include_adult")
        Boolean includeAdult,
        String language,
        @JsonProperty("primary_release_year")
        String primaryReleaseYear,
        Integer page,
        String region,
        String year
) {
    /**
     * Applies defaults for optional search parameters.
     */
    public TmdbMovieSearchRequest {
        includeAdult = includeAdult != null ? includeAdult : Boolean.FALSE;
        language = (language == null || language.isBlank()) ? "en-US" : language;
        page = page != null ? page : 1;
    }
}




package com.michelenuovo.movieagent.dto.tmdb.search.media;

/**
 * Unified request DTO for TMDB-backed media discovery.
 *
 * <p>This request acts as the generic search contract used by the AI tool layer before the mapper
 * and strategy layers translate it into movie-, TV-, person-, or keyword-specific TMDB requests.
 * It includes shared search fields plus optional filters that only apply to certain media types.
 *
 * @param query free-text search term
 * @param mediaType requested media category; defaults to {@link SearchMediaType#AUTO}
 * @param includeAdult whether adult results should be included; defaults to {@code false}
 * @param language TMDB language code; defaults to {@code en-US}
 * @param page result page number; defaults to {@code 1}
 * @param primaryReleaseYear optional movie filter for primary release year
 * @param firstAirDateYear optional TV filter for first air date year
 * @param region optional movie region filter
 * @param year optional generic year filter used by some TMDB search endpoints
 */
public record TmdbMediaSearchRequest(
        String query,
        SearchMediaType mediaType,
        Boolean includeAdult,
        String language,
        Integer page,
        Integer primaryReleaseYear,
        Integer firstAirDateYear,
        String region,
        Integer year
) {
    /**
     * Applies sensible defaults so callers can omit optional parameters in common search flows.
     */
    public TmdbMediaSearchRequest {
        mediaType = mediaType != null ? mediaType : SearchMediaType.AUTO;
        includeAdult = includeAdult != null ? includeAdult : Boolean.FALSE;
        language = (language == null || language.isBlank()) ? "en-US" : language;
        page = page != null ? page : 1;
    }
}


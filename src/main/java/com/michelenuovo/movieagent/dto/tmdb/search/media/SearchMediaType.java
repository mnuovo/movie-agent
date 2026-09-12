package com.michelenuovo.movieagent.dto.tmdb.search.media;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

/**
 * Supported media categories for unified TMDB searches.
 *
 * <p>This enum is used across the tool, DTO, and strategy layers to express whether a search is
 * targeting movies, TV shows, people, keywords, or an aggregated automatic mode.
 */
public enum SearchMediaType {
    MOVIE,
    TV,
    PERSON,
    KEYWORD,
    AUTO;

    /**
     * Parses a case-insensitive external value into a supported media type.
     *
     * <p>Blank or {@code null} values default to {@link #AUTO} so callers can omit the field when
     * they want broad discovery behavior.
     *
     * @param value incoming serialized media type value
     * @return resolved media type
     */
    @JsonCreator
    public static SearchMediaType from(String value) {
        if (value == null || value.isBlank()) {
            return AUTO;
        }

        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "movie" -> MOVIE;
            case "tv" -> TV;
            case "person" -> PERSON;
            case "keyword" -> KEYWORD;
            case "auto" -> AUTO;
            default -> throw new IllegalArgumentException("mediaType must be one of: movie, tv, person, keyword, auto.");
        };
    }

    /**
    * Serializes the enum using the lowercase value expected by external JSON payloads.
    *
    * @return lowercase media type name
    */
    @JsonValue
    public String value() {
        return name().toLowerCase(Locale.ROOT);
    }
}

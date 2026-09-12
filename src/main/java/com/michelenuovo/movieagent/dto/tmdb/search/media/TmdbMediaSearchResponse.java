package com.michelenuovo.movieagent.dto.tmdb.search.media;

import java.util.List;

/**
 * Normalized response DTO returned by the unified media search layer.
 *
 * <p>This model hides the differences between TMDB movie, TV, person, and keyword payloads so the
 * tool layer can expose one consistent response shape to downstream consumers.
 *
 * @param mediaType media category represented by this response, or {@link SearchMediaType#AUTO}
 * for merged results
 * @param page current result page
 * @param totalPages total number of pages available upstream
 * @param totalResults total number of matching results available upstream
 * @param results normalized result entries
 */
public record TmdbMediaSearchResponse(
        SearchMediaType mediaType,
        int page,
        int totalPages,
        int totalResults,
        List<MediaResult> results
) {
    /**
     * Normalized search-result entry spanning movies, TV shows, people, and keywords.
     *
     * <p>Not every field is applicable to every media type. For example, person results typically
     * use {@code knownForDepartment} and {@code profilePath}, while movie and TV results use
     * release, artwork, genre, and voting fields.
     *
     * @param mediaType type of the represented result
     * @param id TMDB identifier
     * @param title display title or name
     * @param originalTitle original title or original name
     * @param overview plot summary, description, or mapped department text depending on media type
     * @param posterPath poster or mapped profile path depending on source media
     * @param backdropPath backdrop image path when available
     * @param releaseDate release date or first-air date when available
     * @param originalLanguage original language code when available
     * @param genreIds TMDB genre identifiers when available
     * @param popularity TMDB popularity score
     * @param voteAverage average TMDB user vote when available
     * @param voteCount TMDB vote count when available
     * @param knownForDepartment department primarily associated with a person result
     * @param profilePath profile image path primarily associated with a person result
     */
    public record MediaResult(
            SearchMediaType mediaType,
            int id,
            String title,
            String originalTitle,
            String overview,
            String posterPath,
            String backdropPath,
            String releaseDate,
            String originalLanguage,
            List<Integer> genreIds,
            double popularity,
            double voteAverage,
            int voteCount,
            String knownForDepartment,
            String profilePath
    ) {
    }
}

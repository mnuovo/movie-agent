package com.michelenuovo.movieagent.dto.tmdb.search.person;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response DTO returned by the TMDB person search endpoint.
 *
 * @param page current result page
 * @param results matched people for the current page
 * @param totalPages total number of pages available upstream
 * @param totalResults total number of matching people upstream
 */
public record TmdbPersonSearchResponse(
        int page,
        List<PersonResult> results,
        @JsonProperty("total_pages") int totalPages,
        @JsonProperty("total_results") int totalResults
) {
    /**
     * Person result returned by TMDB.
     *
     * @param adult whether the person result is marked as adult-related by TMDB
     * @param gender TMDB numeric gender code
     * @param id TMDB person identifier
     * @param knownForDepartment department the person is primarily known for
     * @param name display name
     * @param originalName original name as returned by TMDB
     * @param popularity TMDB popularity score
     * @param profilePath profile image path
     * @param knownFor representative media credits associated with the person
     */
    public record PersonResult(
            boolean adult,
            int gender,
            int id,
            @JsonProperty("known_for_department") String knownForDepartment,
            String name,
            @JsonProperty("original_name") String originalName,
            double popularity,
            @JsonProperty("profile_path") String profilePath,
            @JsonProperty("known_for") List<KnownForMedia> knownFor
    ) {
    }

    /**
     * Media credit nested inside a TMDB person search result.
     *
     * <p>TMDB includes representative works for a person in the {@code known_for} array. This
     * record captures the subset of fields used by the application while preserving the original
     * JSON field mapping.
     *
     * @param adult whether the credited media is marked as adult-related
     * @param backdropPath backdrop image path
     * @param id TMDB media identifier
     * @param title display title of the credited media
     * @param originalLanguage original language code
     * @param originalTitle original media title
     * @param overview short synopsis
     * @param posterPath poster image path
     * @param mediaType TMDB media type string for the credit
     * @param genreIds TMDB genre identifiers
     * @param popularity TMDB popularity score
     * @param releaseDate release date when available
     * @param video whether the media is flagged as a video
     * @param voteAverage average TMDB vote
     * @param voteCount TMDB vote count
     */
    public record KnownForMedia(
            boolean adult,
            @JsonProperty("backdrop_path") String backdropPath,
            int id,
            String title,
            @JsonProperty("original_language") String originalLanguage,
            @JsonProperty("original_title") String originalTitle,
            String overview,
            @JsonProperty("poster_path") String posterPath,
            @JsonProperty("media_type") String mediaType,
            @JsonProperty("genre_ids") List<Integer> genreIds,
            double popularity,
            @JsonProperty("release_date") String releaseDate,
            boolean video,
            @JsonProperty("vote_average") double voteAverage,
            @JsonProperty("vote_count") int voteCount
    ) {
    }
}


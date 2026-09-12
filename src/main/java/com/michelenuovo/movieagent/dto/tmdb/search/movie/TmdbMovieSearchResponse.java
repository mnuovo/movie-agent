package com.michelenuovo.movieagent.dto.tmdb.search.movie;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response DTO returned by the TMDB movie search endpoint.
 *
 * @param page current result page
 * @param results movie results for the current page
 * @param totalPages total number of pages available upstream
 * @param totalResults total number of matching movies upstream
 */
public record TmdbMovieSearchResponse(
        int page,
        List<MovieResult> results,
        @JsonProperty("total_pages")
        int totalPages,
        @JsonProperty("total_results")
        int totalResults
) {
    /**
     * Movie result returned by TMDB.
     *
     * @param adult whether the movie is marked as adult-related
     * @param backdropPath backdrop image path
     * @param genreIds TMDB genre identifiers
     * @param id TMDB movie identifier
     * @param originalLanguage original language code
     * @param originalTitle original movie title
     * @param overview movie synopsis
     * @param popularity TMDB popularity score
     * @param posterPath poster image path
     * @param releaseDate movie release date
     * @param title localized or display movie title
     * @param video whether the result is flagged as a video
     * @param voteAverage average TMDB vote
     * @param voteCount TMDB vote count
     * @param softcore optional TMDB content classification flag when provided
     */
    public record MovieResult(
            boolean adult,
            @JsonProperty("backdrop_path")
            String backdropPath,
            @JsonProperty("genre_ids")
            List<Integer> genreIds,
            int id,
            @JsonProperty("original_language")
            String originalLanguage,
            @JsonProperty("original_title")
            String originalTitle,
            String overview,
            double popularity,
            @JsonProperty("poster_path")
            String posterPath,
            @JsonProperty("release_date")
            String releaseDate,
            String title,
            boolean video,
            @JsonProperty("vote_average")
            double voteAverage,
            @JsonProperty("vote_count")
            int voteCount,
            Boolean softcore
    ) {
    }
}



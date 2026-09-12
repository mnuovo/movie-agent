package com.michelenuovo.movieagent.dto.tmdb.search.tv;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response DTO returned by the TMDB TV search endpoint.
 *
 * @param page current result page
 * @param results TV results for the current page
 * @param totalPages total number of pages available upstream
 * @param totalResults total number of matching TV shows upstream
 */
public record TmdbTvSearchResponse(
        int page,
        List<TvResult> results,
        @JsonProperty("total_pages")
        int totalPages,
        @JsonProperty("total_results")
        int totalResults
) {
    /**
     * TV search result returned by TMDB.
     *
     * @param adult whether the show is marked as adult-related
     * @param backdropPath backdrop image path
     * @param genreIds TMDB genre identifiers
     * @param id TMDB TV identifier
     * @param originCountry countries of origin reported by TMDB
     * @param originalLanguage original language code
     * @param originalName original show name
     * @param overview show synopsis
     * @param popularity TMDB popularity score
     * @param posterPath poster image path
     * @param firstAirDate first-air date
     * @param name localized or display name
     * @param voteAverage average TMDB vote
     * @param voteCount TMDB vote count
     */
    public record TvResult(
            boolean adult,
            @JsonProperty("backdrop_path")
            String backdropPath,
            @JsonProperty("genre_ids")
            List<Integer> genreIds,
            int id,
            @JsonProperty("origin_country")
            List<String> originCountry,
            @JsonProperty("original_language")
            String originalLanguage,
            @JsonProperty("original_name")
            String originalName,
            String overview,
            double popularity,
            @JsonProperty("poster_path")
            String posterPath,
            @JsonProperty("first_air_date")
            String firstAirDate,
            String name,
            @JsonProperty("vote_average")
            double voteAverage,
            @JsonProperty("vote_count")
            int voteCount
    ) {
    }
}


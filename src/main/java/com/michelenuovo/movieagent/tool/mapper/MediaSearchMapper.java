package com.michelenuovo.movieagent.tool.mapper;

import com.michelenuovo.movieagent.dto.tmdb.search.media.SearchMediaType;
import com.michelenuovo.movieagent.dto.tmdb.search.media.TmdbMediaSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.media.TmdbMediaSearchResponse;
import com.michelenuovo.movieagent.dto.tmdb.search.keyword.TmdbKeywordSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.keyword.TmdbKeywordSearchResponse;
import com.michelenuovo.movieagent.dto.tmdb.search.movie.TmdbMovieSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.movie.TmdbMovieSearchResponse;
import com.michelenuovo.movieagent.dto.tmdb.search.person.TmdbPersonSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.person.TmdbPersonSearchResponse;
import com.michelenuovo.movieagent.dto.tmdb.search.tv.TmdbTvSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.tv.TmdbTvSearchResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Component;

/**
 * Maps between the unified media-search DTOs used by the AI tool layer and the more specific TMDB
 * request/response models used by the client.
 *
 * <p>This component has two main responsibilities:
 * <ul>
 *     <li>convert a generic {@link TmdbMediaSearchRequest} into media-type-specific TMDB request
 *     payloads for movies, TV shows, people, and keywords</li>
 *     <li>normalize the different TMDB response shapes into a single
 *     {@link TmdbMediaSearchResponse} contract that the tool layer can expose consistently</li>
 * </ul>
 *
 * <p>For {@code auto} searches, this mapper also merges multiple normalized responses into one
 * aggregated result sorted by popularity in descending order.
 */
@Component
public class MediaSearchMapper {

    private static final List<Integer> EMPTY_GENRE_IDS = List.of();
    private static final double DEFAULT_POPULARITY = 0.0;
    private static final double DEFAULT_VOTE_AVERAGE = 0.0;
    private static final int DEFAULT_VOTE_COUNT = 0;

    /**
     * Converts a unified media-search request into a TMDB movie-search request.
     *
     * <p>Movie-specific year filters are stored as integers in the generic request model but are
     * represented as strings in the movie-specific TMDB DTO, so this method performs the necessary
     * type conversion while preserving nullable optional filters.
     *
     * @param request generic media request supplied by the tool layer
     * @return movie-specific TMDB request preserving shared and movie-only filters
     */
    public TmdbMovieSearchRequest toMovieRequest(TmdbMediaSearchRequest request) {
        return new TmdbMovieSearchRequest(
                request.query(),
                request.includeAdult(),
                request.language(),
                toStringOrNull(request.primaryReleaseYear()),
                request.page(),
                request.region(),
                toStringOrNull(request.year()));
    }

    /**
     * Converts a unified media-search request into a TMDB TV-search request.
     *
     * @param request generic media request supplied by the tool layer
     * @return TV-specific TMDB request carrying common paging/language fields and optional TV year
     * filters
     */
    public TmdbTvSearchRequest toTvRequest(TmdbMediaSearchRequest request) {
        return new TmdbTvSearchRequest(
                request.query(),
                request.includeAdult(),
                request.language(),
                request.page(),
                request.firstAirDateYear(),
                request.year());
    }

    /**
     * Converts a unified media-search request into a TMDB person-search request.
     *
     * @param request generic media request supplied by the tool layer
     * @return person-specific TMDB request
     */
    public TmdbPersonSearchRequest toPersonRequest(TmdbMediaSearchRequest request) {
        return new TmdbPersonSearchRequest(
                request.query(),
                request.includeAdult(),
                request.language(),
                request.page());
    }

    /**
     * Converts a unified media-search request into a TMDB keyword-search request.
     *
     * <p>Keyword search only uses the query text and page number in this application.
     *
     * @param request generic media request supplied by the tool layer
     * @return keyword-specific TMDB request
     */
    public TmdbKeywordSearchRequest toKeywordRequest(TmdbMediaSearchRequest request) {
        return new TmdbKeywordSearchRequest(
                request.query(),
                request.page());
    }

    /**
     * Maps a TMDB movie-search response into the normalized media-search response model.
     *
     * <p>Movie records preserve title, overview, artwork, release metadata, genre identifiers, and
     * voting metrics while setting person-specific fields to {@code null}.
     *
     * @param response raw TMDB movie-search response
     * @return normalized media-search response tagged as {@link SearchMediaType#MOVIE}
     */
    public TmdbMediaSearchResponse mapMovieResponse(TmdbMovieSearchResponse response) {
        return mapResponse(
                SearchMediaType.MOVIE,
                response.page(),
                response.totalPages(),
                response.totalResults(),
                response.results(),
                result -> mediaResult(
                        SearchMediaType.MOVIE,
                        result.id(),
                        result.title(),
                        result.originalTitle(),
                        result.overview(),
                        result.posterPath(),
                        result.backdropPath(),
                        result.releaseDate(),
                        result.originalLanguage(),
                        result.genreIds(),
                        result.popularity(),
                        result.voteAverage(),
                        result.voteCount(),
                        null,
                        null));
    }

    /**
     * Maps a TMDB TV-search response into the normalized media-search response model.
     *
     * <p>TV records align the TMDB {@code name}/{@code original_name} fields with the generic
     * title fields expected by downstream consumers.
     *
     * @param response raw TMDB TV-search response
     * @return normalized media-search response tagged as {@link SearchMediaType#TV}
     */
    public TmdbMediaSearchResponse mapTvResponse(TmdbTvSearchResponse response) {
        return mapResponse(
                SearchMediaType.TV,
                response.page(),
                response.totalPages(),
                response.totalResults(),
                response.results(),
                result -> mediaResult(
                        SearchMediaType.TV,
                        result.id(),
                        result.name(),
                        result.originalName(),
                        result.overview(),
                        result.posterPath(),
                        result.backdropPath(),
                        result.firstAirDate(),
                        result.originalLanguage(),
                        result.genreIds(),
                        result.popularity(),
                        result.voteAverage(),
                        result.voteCount(),
                        null,
                        null));
    }

    /**
     * Maps a TMDB person-search response into the normalized media-search response model.
     *
     * <p>Because people do not expose movie-style fields such as release date or voting metrics in
     * the same way, this mapper stores the most relevant person data in the generic structure and
     * uses sensible defaults for unsupported fields.
     *
     * @param response raw TMDB person-search response
     * @return normalized media-search response tagged as {@link SearchMediaType#PERSON}
     */
    public TmdbMediaSearchResponse mapPersonResponse(TmdbPersonSearchResponse response) {
        return mapResponse(
                SearchMediaType.PERSON,
                response.page(),
                response.totalPages(),
                response.totalResults(),
                response.results(),
                result -> mediaResult(
                        SearchMediaType.PERSON,
                        result.id(),
                        result.name(),
                        result.originalName(),
                        result.knownForDepartment(),
                        result.profilePath(),
                        null,
                        null,
                        null,
                        EMPTY_GENRE_IDS,
                        result.popularity(),
                        DEFAULT_VOTE_AVERAGE,
                        DEFAULT_VOTE_COUNT,
                        result.knownForDepartment(),
                        result.profilePath()));
    }

    /**
     * Maps a TMDB keyword-search response into the normalized media-search response model.
     *
     * <p>Keyword results contain far less metadata than movies, TV shows, or people, so this
     * method fills unsupported generic fields with neutral defaults.
     *
     * @param response raw TMDB keyword-search response
     * @return normalized media-search response tagged as {@link SearchMediaType#KEYWORD}
     */
    public TmdbMediaSearchResponse mapKeywordResponse(TmdbKeywordSearchResponse response) {
        return mapResponse(
                SearchMediaType.KEYWORD,
                response.page(),
                response.totalPages(),
                response.totalResults(),
                response.results(),
                result -> mediaResult(
                        SearchMediaType.KEYWORD,
                        result.id(),
                        result.name(),
                        result.name(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        EMPTY_GENRE_IDS,
                        DEFAULT_POPULARITY,
                        DEFAULT_VOTE_AVERAGE,
                        DEFAULT_VOTE_COUNT,
                        null,
                        null));
    }

    /**
     * Merges multiple normalized media responses into one aggregated {@code auto} response.
     *
     * <p>The merge behavior is intentionally simple and deterministic:
     * <ul>
     *     <li>all result items are concatenated into a single list</li>
     *     <li>{@code page} and {@code totalPages} keep the maximum observed values</li>
     *     <li>{@code totalResults} is the sum across all response types</li>
     *     <li>the final merged list is sorted by popularity descending</li>
     * </ul>
     *
     * <p>This allows the caller to present a cross-media search result ordered by general relevance
     * without needing to know the original TMDB response shape for each media type.
     *
     * @param responses normalized per-media responses to combine
     * @return aggregated response tagged as {@link SearchMediaType#AUTO}
     */
    public TmdbMediaSearchResponse mergeAutoResponse(List<TmdbMediaSearchResponse> responses) {
        List<TmdbMediaSearchResponse.MediaResult> merged = new ArrayList<>();
        int page = 0;
        int totalPages = 0;
        int totalResults = 0;

        for (TmdbMediaSearchResponse response : responses) {
            merged.addAll(response.results());
            page = Math.max(page, response.page());
            totalPages = Math.max(totalPages, response.totalPages());
            totalResults += response.totalResults();
        }

        merged.sort(Comparator.comparingDouble(TmdbMediaSearchResponse.MediaResult::popularity).reversed());
        return new TmdbMediaSearchResponse(SearchMediaType.AUTO, page, totalPages, totalResults, merged);
    }

    private static String toStringOrNull(Integer value) {
        return value == null ? null : String.valueOf(value);
    }

    private <T> TmdbMediaSearchResponse mapResponse(
            SearchMediaType mediaType,
            int page,
            int totalPages,
            int totalResults,
            List<T> sourceResults,
            Function<T, TmdbMediaSearchResponse.MediaResult> itemMapper
    ) {
        List<TmdbMediaSearchResponse.MediaResult> results = sourceResults.stream()
                .map(itemMapper)
                .toList();
        return new TmdbMediaSearchResponse(mediaType, page, totalPages, totalResults, results);
    }

    private TmdbMediaSearchResponse.MediaResult mediaResult(
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
        return new TmdbMediaSearchResponse.MediaResult(
                mediaType,
                id,
                title,
                originalTitle,
                overview,
                posterPath,
                backdropPath,
                releaseDate,
                originalLanguage,
                genreIds,
                popularity,
                voteAverage,
                voteCount,
                knownForDepartment,
                profilePath);
    }
}

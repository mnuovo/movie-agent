package com.michelenuovo.movieagent.tool.strategy;

import com.michelenuovo.movieagent.client.tmdb.TmdbClient;
import com.michelenuovo.movieagent.dto.tmdb.search.keyword.TmdbKeywordSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.keyword.TmdbKeywordSearchResponse;
import com.michelenuovo.movieagent.dto.tmdb.search.media.SearchMediaType;
import com.michelenuovo.movieagent.dto.tmdb.search.media.TmdbMediaSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.media.TmdbMediaSearchResponse;
import com.michelenuovo.movieagent.dto.tmdb.search.movie.TmdbMovieSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.movie.TmdbMovieSearchResponse;
import com.michelenuovo.movieagent.dto.tmdb.search.person.TmdbPersonSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.person.TmdbPersonSearchResponse;
import com.michelenuovo.movieagent.dto.tmdb.search.tv.TmdbTvSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.tv.TmdbTvSearchResponse;
import com.michelenuovo.movieagent.properties.TmdbProperties;
import com.michelenuovo.movieagent.tool.mapper.MediaSearchMapper;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AutoMediaSearchStrategyTest {

    @Test
    void mergesReactiveSearchesWithoutBlockingAndOrdersByPopularity() {
        CountingTmdbClient tmdbClient = new CountingTmdbClient(
                movieResponse(),
                tvResponse(),
                personResponse(),
                keywordResponse());
        AutoMediaSearchStrategy strategy = new AutoMediaSearchStrategy(tmdbClient, new MediaSearchMapper());

        TmdbMediaSearchResponse response = strategy.search(request()).block();

        assertNotNull(response);
        assertEquals(SearchMediaType.AUTO, response.mediaType());
        assertEquals(3, response.page());
        assertEquals(5, response.totalPages());
        assertEquals(18, response.totalResults());
        assertEquals(4, response.results().size());
        assertEquals(SearchMediaType.MOVIE, response.results().get(0).mediaType());
        assertEquals(SearchMediaType.TV, response.results().get(1).mediaType());
        assertEquals(SearchMediaType.PERSON, response.results().get(2).mediaType());
        assertEquals(SearchMediaType.KEYWORD, response.results().get(3).mediaType());
        assertEquals(1, tmdbClient.movieCalls.get());
        assertEquals(1, tmdbClient.tvCalls.get());
        assertEquals(1, tmdbClient.personCalls.get());
        assertEquals(1, tmdbClient.keywordCalls.get());
    }

    private static TmdbMediaSearchRequest request() {
        return new TmdbMediaSearchRequest("matrix", SearchMediaType.AUTO, false, "en-US", 1, null, null, null, null);
    }

    private static TmdbMovieSearchResponse movieResponse() {
        return new TmdbMovieSearchResponse(
                2,
                List.of(new TmdbMovieSearchResponse.MovieResult(
                        false,
                        "movie-backdrop",
                        List.of(1),
                        101,
                        "en",
                        "movie-original",
                        "movie overview",
                        40.0,
                        "movie-poster",
                        "2020-01-01",
                        "Movie Title",
                        false,
                        7.1,
                        71,
                        null)),
                4,
                8);
    }

    private static TmdbTvSearchResponse tvResponse() {
        return new TmdbTvSearchResponse(
                3,
                List.of(new TmdbTvSearchResponse.TvResult(
                        false,
                        "tv-backdrop",
                        List.of(2),
                        202,
                        List.of("US"),
                        "en",
                        "tv-original",
                        "tv overview",
                        30.0,
                        "tv-poster",
                        "2021-02-02",
                        "TV Title",
                        8.2,
                        82)),
                5,
                6);
    }

    private static TmdbPersonSearchResponse personResponse() {
        return new TmdbPersonSearchResponse(
                1,
                List.of(new TmdbPersonSearchResponse.PersonResult(
                        false,
                        1,
                        303,
                        "Acting",
                        "Person Title",
                        "Person Original",
                        20.0,
                        "person-profile",
                        List.of())),
                2,
                3);
    }

    private static TmdbKeywordSearchResponse keywordResponse() {
        return new TmdbKeywordSearchResponse(
                1,
                List.of(new TmdbKeywordSearchResponse.KeywordResult(404, "keyword-title")),
                1,
                1);
    }

    private static final class CountingTmdbClient extends TmdbClient {
        private final TmdbMovieSearchResponse movieResponse;
        private final TmdbTvSearchResponse tvResponse;
        private final TmdbPersonSearchResponse personResponse;
        private final TmdbKeywordSearchResponse keywordResponse;
        private final AtomicInteger movieCalls = new AtomicInteger();
        private final AtomicInteger tvCalls = new AtomicInteger();
        private final AtomicInteger personCalls = new AtomicInteger();
        private final AtomicInteger keywordCalls = new AtomicInteger();

        private CountingTmdbClient(
                TmdbMovieSearchResponse movieResponse,
                TmdbTvSearchResponse tvResponse,
                TmdbPersonSearchResponse personResponse,
                TmdbKeywordSearchResponse keywordResponse) {
            super(WebClient.builder(), new TmdbProperties("http://localhost", null, null, null, null, null));
            this.movieResponse = movieResponse;
            this.tvResponse = tvResponse;
            this.personResponse = personResponse;
            this.keywordResponse = keywordResponse;
        }

        @Override
        public Mono<TmdbMovieSearchResponse> searchMovies(TmdbMovieSearchRequest request) {
            movieCalls.incrementAndGet();
            return Mono.just(movieResponse);
        }

        @Override
        public Mono<TmdbTvSearchResponse> searchTv(TmdbTvSearchRequest request) {
            tvCalls.incrementAndGet();
            return Mono.just(tvResponse);
        }

        @Override
        public Mono<TmdbPersonSearchResponse> searchPerson(TmdbPersonSearchRequest request) {
            personCalls.incrementAndGet();
            return Mono.just(personResponse);
        }

        @Override
        public Mono<TmdbKeywordSearchResponse> searchKeyword(TmdbKeywordSearchRequest request) {
            keywordCalls.incrementAndGet();
            return Mono.just(keywordResponse);
        }
    }
}


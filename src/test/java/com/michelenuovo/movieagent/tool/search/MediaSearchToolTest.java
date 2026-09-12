package com.michelenuovo.movieagent.tool.search;

import com.michelenuovo.movieagent.dto.tmdb.search.media.SearchMediaType;
import com.michelenuovo.movieagent.dto.tmdb.search.media.TmdbMediaSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.media.TmdbMediaSearchResponse;
import com.michelenuovo.movieagent.tool.strategy.MediaSearchStrategy;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MediaSearchToolTest {

    @Test
    void routesToMatchingStrategyBasedOnMediaType() {
        TmdbMediaSearchResponse movieResponse = response(SearchMediaType.MOVIE);
        TmdbMediaSearchResponse tvResponse = response(SearchMediaType.TV);
        AtomicInteger movieCalls = new AtomicInteger();
        AtomicInteger tvCalls = new AtomicInteger();

        MediaSearchStrategy movieStrategy = new StubStrategy(SearchMediaType.MOVIE, movieResponse, movieCalls);
        MediaSearchStrategy tvStrategy = new StubStrategy(SearchMediaType.TV, tvResponse, tvCalls);

        MediaSearchTool tool = new MediaSearchTool(List.of(movieStrategy, tvStrategy));

        TmdbMediaSearchResponse response = tool.mediaSearchTool(
                new TmdbMediaSearchRequest("breaking bad", SearchMediaType.TV, false, "en-US", 1, null, null, null, null))
                .block();

        assertSame(tvResponse, response);
        assertEquals(0, movieCalls.get());
        assertEquals(1, tvCalls.get());
    }

    @Test
    void routesPersonSearchToPersonStrategy() {
        TmdbMediaSearchResponse personResponse = response(SearchMediaType.PERSON);
        AtomicInteger personCalls = new AtomicInteger();

        MediaSearchStrategy personStrategy = new StubStrategy(SearchMediaType.PERSON, personResponse, personCalls);
        MediaSearchTool tool = new MediaSearchTool(List.of(personStrategy));

        TmdbMediaSearchResponse response = tool.mediaSearchTool(
                new TmdbMediaSearchRequest("Tom Hanks", SearchMediaType.PERSON, false, "en-US", 1, null, null, null, null))
                .block();

        assertSame(personResponse, response);
        assertEquals(1, personCalls.get());
    }

    @Test
    void routesKeywordSearchToKeywordStrategy() {
        TmdbMediaSearchResponse keywordResponse = response(SearchMediaType.KEYWORD);
        AtomicInteger keywordCalls = new AtomicInteger();

        MediaSearchStrategy keywordStrategy = new StubStrategy(SearchMediaType.KEYWORD, keywordResponse, keywordCalls);
        MediaSearchTool tool = new MediaSearchTool(List.of(keywordStrategy));

        TmdbMediaSearchResponse response = tool.mediaSearchTool(
                new TmdbMediaSearchRequest("lost", SearchMediaType.KEYWORD, false, "en-US", 1, null, null, null, null))
                .block();

        assertSame(keywordResponse, response);
        assertEquals(1, keywordCalls.get());
    }

    @Test
    void rejectsDuplicateStrategyRegistration() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                MediaSearchToolTest::buildToolWithDuplicateStrategies);

        assertEquals("Duplicate media search strategy registered for MOVIE", exception.getMessage());
    }

    private static void buildToolWithDuplicateStrategies() {
        MediaSearchStrategy first = new StubStrategy(SearchMediaType.MOVIE, response(SearchMediaType.MOVIE), new AtomicInteger());
        MediaSearchStrategy duplicate = new StubStrategy(SearchMediaType.MOVIE, response(SearchMediaType.MOVIE), new AtomicInteger());
        new MediaSearchTool(List.of(first, duplicate));
    }

    private static TmdbMediaSearchResponse response(SearchMediaType mediaType) {
        return new TmdbMediaSearchResponse(mediaType, 1, 1, 0, List.of());
    }

    private static final class StubStrategy implements MediaSearchStrategy {
        private final SearchMediaType mediaType;
        private final TmdbMediaSearchResponse response;
        private final AtomicInteger calls;

        private StubStrategy(SearchMediaType mediaType, TmdbMediaSearchResponse response, AtomicInteger calls) {
            this.mediaType = mediaType;
            this.response = response;
            this.calls = calls;
        }

        @Override
        public SearchMediaType mediaType() {
            return mediaType;
        }

        @Override
        public Mono<TmdbMediaSearchResponse> search(TmdbMediaSearchRequest request) {
            calls.incrementAndGet();
            return Mono.just(response);
        }
    }
}



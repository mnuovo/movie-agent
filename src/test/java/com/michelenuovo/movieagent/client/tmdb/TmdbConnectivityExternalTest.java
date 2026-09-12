package com.michelenuovo.movieagent.client.tmdb;

import com.michelenuovo.movieagent.dto.tmdb.search.person.TmdbPersonSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.tv.TmdbTvSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.keyword.TmdbKeywordSearchRequest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Tag("external")
@EnabledIfEnvironmentVariable(named = "RUN_EXTERNAL_TESTS", matches = "true")
class TmdbConnectivityExternalTest {

    @Autowired
    private TmdbClient tmdbClient;

    @Test
    void fetchesMovieFromTmdbUsingConfiguredCredentialsAndTls() {
        String responseBody = tmdbClient.getMovieById(11).block(Duration.ofSeconds(20));

        assertNotNull(responseBody);
        assertTrue(responseBody.contains("\"id\":11"));
        assertTrue(responseBody.contains("\"title\""));
    }

    @Test
    void searchesTvShowsUsingConfiguredCredentialsAndTls() {
        TmdbTvSearchRequest request = new TmdbTvSearchRequest("Breaking Bad", false, "en-US", 1, null, null);

        var response = tmdbClient.searchTv(request).block(Duration.ofSeconds(20));

        assertNotNull(response);
        assertNotNull(response.results());
        assertTrue(response.results().stream().anyMatch(result -> result.name() != null && !result.name().isBlank()));
    }

    @Test
    void searchesPeopleUsingConfiguredCredentialsAndTls() {
        TmdbPersonSearchRequest request = new TmdbPersonSearchRequest("Tom Hanks", false, "en-US", 1);

        var response = tmdbClient.searchPerson(request).block(Duration.ofSeconds(20));

        assertNotNull(response);
        assertNotNull(response.results());
        assertTrue(response.results().stream().anyMatch(result -> result.name() != null && !result.name().isBlank()));
    }

    @Test
    void searchesKeywordsUsingConfiguredCredentialsAndTls() {
        TmdbKeywordSearchRequest request = new TmdbKeywordSearchRequest("lost", 1);

        var response = tmdbClient.searchKeyword(request).block(Duration.ofSeconds(20));

        assertNotNull(response);
        assertNotNull(response.results());
        assertTrue(response.results().stream().anyMatch(result -> result.name() != null && !result.name().isBlank()));
    }
}

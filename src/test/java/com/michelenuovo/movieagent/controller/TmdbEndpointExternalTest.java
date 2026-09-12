package com.michelenuovo.movieagent.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("external")
@EnabledIfEnvironmentVariable(named = "RUN_EXTERNAL_TESTS", matches = "true")
class TmdbEndpointExternalTest {

    @LocalServerPort
    private int port;

    @Test
    void returnsUpstreamNotFoundForMissingMovie() {
        WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build()
                .get()
                .uri("/tmdb/movie/1")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .value(body -> assertFalse(body == null || body.isBlank()));
    }

    @Test
    void returnsUpstreamNotFoundForMissingTvShow() {
        WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build()
                .get()
                .uri("/tmdb/tv/1")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .value(body -> assertFalse(body == null || body.isBlank()));
    }
}


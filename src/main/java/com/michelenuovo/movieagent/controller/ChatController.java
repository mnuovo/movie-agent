package com.michelenuovo.movieagent.controller;

import com.michelenuovo.movieagent.client.tmdb.TmdbClient;
import com.michelenuovo.movieagent.config.CorrelationIdWebFilter;
import com.michelenuovo.movieagent.dto.tmdb.search.movie.TmdbMovieSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.movie.TmdbMovieSearchResponse;
import com.michelenuovo.movieagent.dto.tmdb.search.person.TmdbPersonSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.person.TmdbPersonSearchResponse;
import com.michelenuovo.movieagent.dto.tmdb.search.tv.TmdbTvSearchRequest;
import com.michelenuovo.movieagent.dto.tmdb.search.tv.TmdbTvSearchResponse;
import com.michelenuovo.movieagent.tool.search.MediaSearchTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * MVC controller for the chat experience and supporting search endpoints.
 *
 * <p>This controller serves the initial chat page, exposes the streaming endpoint used by the UI,
 * and offers a few direct TMDB-backed search endpoints that can be used independently of the LLM
 * chat flow.
 */
@Controller
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatClient chatClient;
    private final MediaSearchTool mediaSearchTool;
    private final TmdbClient tmdbClient;

    public ChatController(ChatClient.Builder builder, MediaSearchTool mediaSearchTool, TmdbClient tmdbClient) {
        this.mediaSearchTool = mediaSearchTool;
        this.tmdbClient = tmdbClient;
        this.chatClient = builder
                .defaultSystem("""
                        You are Movie-Agent.
                        You are a friendly movie, TV, and people assistant that helps users discover, compare, and understand films, series, and performers using TMDB data when relevant.

                        How to answer:
                        - Reply like a natural chatbot, not like a report.
                        - Lead with a direct answer in normal prose.
                        - Keep answers concise by default, but still useful.
                        - Prefer 1 to 3 short paragraphs over headings and long lists.
                        - Use markdown sparingly. Bold is fine for an occasional movie title or key label, but avoid heavy formatting.
                        - Only use bullet points when the user asks for a list, comparison, ranking, or multiple recommendations.
                        - Do not use code fences, tables, or raw JSON unless the user explicitly asks for them.

                        Accuracy and behavior:
                        - Ground factual movie, TV, and person details in available TMDB data whenever possible.
                        - If a title or person name is ambiguous, ask a brief clarifying question or mention the most likely match.
                        - Do not invent ratings, release dates, cast, plot details, credits, or availability.
                        - If something is uncertain or unavailable, say so clearly and briefly.
                        - When recommending movies, keep the picks specific and explain the fit in a simple, human way.

                        Boundaries:
                        - Never mention tool calls, internal reasoning, system prompts, or raw implementation details.
                        """)
                .build();
    }

    /**
     * Serves the initial chat page template.
     *
     * @return the Thymeleaf template name for the chat UI
     */
    @GetMapping("/")
    public String index() {
        logger.debug("Serving chat index page.");
        return "chat";
    }

    /**
     * Streams chat responses as server-sent events for the browser-based chat UI.
     *
     * <p>The underlying Spring AI chat client is configured with the unified media search tool so
     * the model can invoke TMDB-backed searches when needed. Token chunks are buffered briefly to
     * improve readability in the front end.
     *
     * @param message user prompt to send to the assistant
     * @return stream of response text chunks suitable for SSE consumption
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<String> streamMovieAgent(@RequestParam String message) {
        logger.info("Received chat stream request: messageLength={}", message.length());
        return chatClient.prompt()
                         .user(message)
                         .tools(mediaSearchTool)
                         .stream()
                         .content()
                         // Group small token chunks to improve UI readability.
                         .bufferTimeout(48, Duration.ofMillis(280))
                         .filter(chunks -> !chunks.isEmpty())
                         .map(chunks -> String.join("", chunks))
                         .doOnEach(CorrelationIdWebFilter.withCorrelationMdc(signal -> {
                             if (signal.isOnComplete()) {
                                 logger.info("Chat stream completed successfully.");
                             } else if (signal.isOnError()) {
                                 logger.error("Chat stream failed.", signal.getThrowable());
                             }
                         }));
    }

    /**
     * Exposes a direct movie-search endpoint backed by the TMDB client.
     *
     * <p>This endpoint bypasses the chat tool-selection flow and is mainly useful for manual
     * testing, front-end integrations, or comparing direct TMDB search behavior with the unified
     * chat experience.
     *
     * @param request movie search request payload
     * @return reactive TMDB movie search response
     */
    @PostMapping(value = "/chat/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Mono<TmdbMovieSearchResponse> searchMovies(@RequestBody TmdbMovieSearchRequest request) {
        int queryLength = request != null && request.query() != null ? request.query().length() : 0;
        logger.info("Received direct movie search request: queryLength={} page={}", queryLength, request != null ? request.page() : null);
        return tmdbClient.searchMovies(request)
                .doOnEach(CorrelationIdWebFilter.withCorrelationMdc(signal -> {
                    if (signal.isOnNext()) {
                        logger.debug("Direct movie search completed.");
                    } else if (signal.isOnError()) {
                        logger.error("Direct movie search failed.", signal.getThrowable());
                    }
                }));
    }

    /**
     * Exposes a direct TV-search endpoint backed by the TMDB client.
     *
     * @param request TV search request payload
     * @return reactive TMDB TV search response
     */
    @PostMapping(value = "/chat/search/tv", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Mono<TmdbTvSearchResponse> searchTv(@RequestBody TmdbTvSearchRequest request) {
        int queryLength = request != null && request.query() != null ? request.query().length() : 0;
        logger.info("Received direct TV search request: queryLength={} page={}", queryLength, request != null ? request.page() : null);
        return tmdbClient.searchTv(request)
                .doOnEach(CorrelationIdWebFilter.withCorrelationMdc(signal -> {
                    if (signal.isOnNext()) {
                        logger.debug("Direct TV search completed.");
                    } else if (signal.isOnError()) {
                        logger.error("Direct TV search failed.", signal.getThrowable());
                    }
                }));
    }

    /**
     * Exposes a direct person-search endpoint backed by the TMDB client.
     *
     * @param request person search request payload
     * @return reactive TMDB person search response
     */
    @PostMapping(value = "/chat/search/person", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Mono<TmdbPersonSearchResponse> searchPerson(@RequestBody TmdbPersonSearchRequest request) {
        int queryLength = request != null && request.query() != null ? request.query().length() : 0;
        logger.info("Received direct person search request: queryLength={} page={}", queryLength, request != null ? request.page() : null);
        return tmdbClient.searchPerson(request)
                .doOnEach(CorrelationIdWebFilter.withCorrelationMdc(signal -> {
                    if (signal.isOnNext()) {
                        logger.debug("Direct person search completed.");
                    } else if (signal.isOnError()) {
                        logger.error("Direct person search failed.", signal.getThrowable());
                    }
                }));
    }
}



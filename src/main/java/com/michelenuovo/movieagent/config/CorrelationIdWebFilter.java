package com.michelenuovo.movieagent.config;

import java.util.UUID;
import java.util.function.Consumer;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.core.publisher.SignalType;

/**
 * Correlates logs for each inbound request using {@code X-Correlation-Id}.
 *
 * <p>When a caller does not provide the header, the filter generates a new UUID, stores it in
 * Reactor context, and echoes it on the response so clients can reference it.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdWebFilter implements WebFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    public static final String CORRELATION_ID_CONTEXT_KEY = CorrelationIdWebFilter.class.getName() + ".correlationId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String incomingCorrelationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        String correlationId = StringUtils.hasText(incomingCorrelationId) ? incomingCorrelationId : UUID.randomUUID().toString();

        exchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, correlationId);

        return chain.filter(exchange)
                .doFirst(() -> MDC.put(CORRELATION_ID_MDC_KEY, correlationId))
                // Reactor always provides a terminal SignalType here; we keep it in the
                // clearMdc signature to make future signal-specific cleanup explicit.
                .doFinally(signalType -> clearMdc(signalType, correlationId))
                .contextWrite(context -> context.put(CORRELATION_ID_CONTEXT_KEY, correlationId));
    }

    /**
     * Clears MDC at stream termination.
     *
     * <p>{@code signalType} is intentionally accepted (even if currently unused) because
     * {@link Mono#doFinally(java.util.function.Consumer)} emits completion, error, and cancel
     * terminal signals. Keeping it here documents that cleanup runs for all terminal paths and
     * leaves a clear extension point if we ever differentiate behavior by signal type.
     */
    private static void clearMdc(SignalType signalType, String correlationId) {
        if (StringUtils.hasText(correlationId)) {
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }

    /**
     * Executes a reactive logging callback with the correlation id copied from Reactor context
     * into MDC for the duration of the callback.
     */
    public static <T> Consumer<Signal<T>> withCorrelationMdc(Consumer<Signal<T>> consumer) {
        return signal -> {
            String correlationId = signal.getContextView().getOrDefault(CORRELATION_ID_CONTEXT_KEY, null);
            if (StringUtils.hasText(correlationId)) {
                MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
            }
            try {
                consumer.accept(signal);
            } finally {
                if (StringUtils.hasText(correlationId)) {
                    MDC.remove(CORRELATION_ID_MDC_KEY);
                }
            }
        };
    }
}

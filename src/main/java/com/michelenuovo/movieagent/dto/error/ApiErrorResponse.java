package com.michelenuovo.movieagent.dto.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

/**
 * Stable JSON error contract returned by global exception handling.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String correlationId,
        List<FieldViolation> violations
) {

    /**
     * Field-level validation violation details.
     */
    public record FieldViolation(String field, String message) {
    }
}


package com.michelenuovo.movieagent.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lightweight operational controller for service health checks.
 *
 * <p>This endpoint is intended for simple smoke tests, local verification, or external uptime
 * probes.
 */
@RestController
class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    /**
     * Returns a minimal health payload indicating that the application is up.
     *
     * @return immutable status map with a single {@code status=UP} entry
     */
    @GetMapping("/health")
    Map<String, String> health() {
        logger.debug("Health endpoint invoked.");
        return Map.of("status", "UP");
    }
}



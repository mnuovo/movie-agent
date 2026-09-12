package com.michelenuovo.movieagent.config;

import com.michelenuovo.movieagent.properties.TmdbProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that registers {@link TmdbProperties} for constructor-style property
 * binding.
 *
 * <p>This keeps TMDB integration settings centralized and injectable throughout the application,
 * especially in the reactive client layer.
 */
@Configuration
@EnableConfigurationProperties(TmdbProperties.class)
class TmdbConfig {
}



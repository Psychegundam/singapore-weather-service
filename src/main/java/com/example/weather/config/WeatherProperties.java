package com.example.weather.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Strongly-typed configuration bound from the {@code weather.*} namespace in
 * application.yml. Keeping all tunables here means a new developer changes
 * behavior (timeouts, cache TTL, API keys) in one obvious place.
 */
@ConfigurationProperties(prefix = "weather")
public record WeatherProperties(
        Cache cache,
        Provider weatherstack,
        Provider openweathermap) {

    public record Cache(
            /** How long a reading is considered fresh before a refresh is attempted. */
            Duration ttl) {
    }

    public record Provider(
            boolean enabled,
            String baseUrl,
            String apiKey,
            Duration connectTimeout,
            Duration readTimeout) {
    }
}

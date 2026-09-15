package com.example.weather.provider;

import com.example.weather.domain.Weather;

/**
 * A source of current weather data.
 *
 * <p>Adding a new provider is deliberately a one-file change: implement this
 * interface, annotate the bean with {@code @Order} to place it in the failover
 * chain, and the {@code WeatherService} will pick it up automatically. No other
 * code needs to change.
 */
public interface WeatherProvider {

    /**
     * Human-readable provider name, used for logging and diagnostics.
     */
    String name();

    /**
     * Fetch the current weather for the given city.
     *
     * @param city the city to query
     * @return normalized weather data
     * @throws WeatherProviderException if the provider is unreachable, returns
     *                                  an error, or the payload cannot be parsed
     */
    Weather fetchCurrent(String city);
}

package com.example.weather.provider.openweathermap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Subset of the OpenWeatherMap {@code /data/2.5/weather} payload we care about.
 * See https://openweathermap.org/current
 *
 * <p>Queried with {@code units=metric}: {@code main.temp} is in Celsius and
 * {@code wind.speed} is in metres/second (converted to km/h by the provider).
 * The {@code cod} field is {@code 200} on success and an error code otherwise;
 * it is typed as String because the API returns it as both a number and a string.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenWeatherMapResponse(
        String cod,
        String message,
        Main main,
        Wind wind) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Main(Double temp) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Wind(Double speed) {
    }
}

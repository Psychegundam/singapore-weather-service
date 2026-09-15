package com.example.weather.web;

import com.example.weather.domain.Weather;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * The public, unified JSON contract returned to callers.
 *
 * <p>Field names are locked to the API spec via {@link JsonProperty} and are
 * intentionally decoupled from the internal {@link Weather} model so the domain
 * can evolve without breaking clients.
 */
@JsonPropertyOrder({"wind_speed", "temperature_degrees"})
public record WeatherResponse(
        @JsonProperty("wind_speed") long windSpeed,
        @JsonProperty("temperature_degrees") long temperatureDegrees) {

    public static WeatherResponse from(Weather weather) {
        return new WeatherResponse(
                Math.round(weather.windSpeedKmh()),
                Math.round(weather.temperatureDegreesCelsius()));
    }
}

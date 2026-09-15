package com.example.weather.provider.weatherstack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Subset of the Weatherstack {@code /current} payload we care about.
 * See https://weatherstack.com/documentation
 *
 * <p>Default units are metric: {@code temperature} in Celsius, {@code wind_speed}
 * in km/h.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherstackResponse(
        Boolean success,
        Error error,
        Current current) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Current(
            Double temperature,
            @JsonProperty("wind_speed") Double windSpeed) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Error(Integer code, String type, String info) {
    }
}

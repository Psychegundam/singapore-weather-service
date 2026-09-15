package com.example.weather.web;

import com.example.weather.domain.Weather;
import com.example.weather.service.WeatherService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public HTTP API.
 *
 * <p>{@code GET /v1/weather?city=singapore} returns the unified payload. The
 * {@code city} parameter defaults to {@code singapore}; the service is scoped to
 * Singapore per the spec, but the parameter is kept so the contract can grow
 * without a breaking change.
 */
@RestController
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping(value = "/v1/weather", produces = MediaType.APPLICATION_JSON_VALUE)
    public WeatherResponse getWeather(@RequestParam(defaultValue = "singapore") String city) {
        Weather weather = weatherService.getWeather(city);
        return WeatherResponse.from(weather);
    }
}

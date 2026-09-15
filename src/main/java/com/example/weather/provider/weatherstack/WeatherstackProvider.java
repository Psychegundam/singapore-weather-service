package com.example.weather.provider.weatherstack;

import com.example.weather.config.RestClientFactory;
import com.example.weather.config.WeatherProperties;
import com.example.weather.domain.Weather;
import com.example.weather.provider.WeatherProvider;
import com.example.weather.provider.WeatherProviderException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Primary provider: Weatherstack.
 *
 * <p>{@code @Order(1)} makes this the first provider tried. Returns temperature
 * in Celsius and wind speed in km/h natively, so no unit conversion is needed.
 */
@Component
@Order(1)
@ConditionalOnProperty(prefix = "weather.weatherstack", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WeatherstackProvider implements WeatherProvider {

    private final RestClient client;
    private final String apiKey;

    public WeatherstackProvider(RestClientFactory factory, WeatherProperties properties) {
        this.client = factory.create(properties.weatherstack());
        this.apiKey = properties.weatherstack().apiKey();
    }

    @Override
    public String name() {
        return "weatherstack";
    }

    @Override
    public Weather fetchCurrent(String city) {
        WeatherstackResponse response;
        try {
            response = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/current")
                            .queryParam("access_key", apiKey)
                            .queryParam("units", "m")
                            .queryParam("query", "Singapore")
                            .build())
                    .retrieve()
                    .body(WeatherstackResponse.class);
        } catch (RuntimeException e) {
            throw new WeatherProviderException("weatherstack request failed", e);
        }

        if (response == null) {
            throw new WeatherProviderException("weatherstack returned an empty body");
        }
        if (Boolean.FALSE.equals(response.success()) || response.error() != null) {
            WeatherstackResponse.Error error = response.error();
            throw new WeatherProviderException("weatherstack error: "
                    + (error != null ? error.info() : "unknown"));
        }
        WeatherstackResponse.Current current = response.current();
        if (current == null || current.temperature() == null || current.windSpeed() == null) {
            throw new WeatherProviderException("weatherstack payload missing current temperature/wind_speed");
        }

        return new Weather(current.temperature(), current.windSpeed());
    }
}

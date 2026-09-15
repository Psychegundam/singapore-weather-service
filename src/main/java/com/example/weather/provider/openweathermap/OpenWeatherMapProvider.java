package com.example.weather.provider.openweathermap;

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
 * Failover provider: OpenWeatherMap.
 *
 * <p>{@code @Order(2)} places it second in the chain. Queried with
 * {@code units=metric} for Celsius; wind speed arrives in m/s and is converted
 * to km/h so the unified response is consistent across providers.
 */
@Component
@Order(2)
@ConditionalOnProperty(prefix = "weather.openweathermap", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OpenWeatherMapProvider implements WeatherProvider {

    private static final double MS_TO_KMH = 3.6;

    private final RestClient client;
    private final String apiKey;

    public OpenWeatherMapProvider(RestClientFactory factory, WeatherProperties properties) {
        this.client = factory.create(properties.openweathermap());
        this.apiKey = properties.openweathermap().apiKey();
    }

    @Override
    public String name() {
        return "openweathermap";
    }

    @Override
    public Weather fetchCurrent(String city) {
        OpenWeatherMapResponse response;
        try {
            response = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/data/2.5/weather")
                            .queryParam("q", "singapore,SG")
                            .queryParam("units", "metric")
                            .queryParam("appid", apiKey)
                            .build())
                    .retrieve()
                    .body(OpenWeatherMapResponse.class);
        } catch (RuntimeException e) {
            throw new WeatherProviderException("openweathermap request failed", e);
        }

        if (response == null) {
            throw new WeatherProviderException("openweathermap returned an empty body");
        }
        if (response.cod() != null && !"200".equals(response.cod())) {
            throw new WeatherProviderException("openweathermap error: " + response.message());
        }
        if (response.main() == null || response.main().temp() == null
                || response.wind() == null || response.wind().speed() == null) {
            throw new WeatherProviderException("openweathermap payload missing temp/wind speed");
        }

        return new Weather(response.main().temp(), response.wind().speed() * MS_TO_KMH);
    }
}

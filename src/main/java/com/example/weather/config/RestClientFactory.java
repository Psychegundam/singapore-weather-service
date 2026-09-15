package com.example.weather.config;

import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Builds {@link RestClient} instances with per-provider timeouts.
 *
 * <p>Short, explicit timeouts are essential to fast failover: a hung provider
 * must not hold a request thread long enough to delay switching to the backup.
 */
@Component
public class RestClientFactory {

    public RestClient create(WeatherProperties.Provider provider) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(provider.connectTimeout())
                .withReadTimeout(provider.readTimeout());

        return RestClient.builder()
                .requestFactory(ClientHttpRequestFactories.get(settings))
                .baseUrl(provider.baseUrl())
                .build();
    }
}

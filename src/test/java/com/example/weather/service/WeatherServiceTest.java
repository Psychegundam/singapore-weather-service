package com.example.weather.service;

import com.example.weather.config.WeatherProperties;
import com.example.weather.domain.Weather;
import com.example.weather.provider.WeatherProvider;
import com.example.weather.provider.WeatherProviderException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeatherServiceTest {

    private static WeatherProperties props(Duration ttl) {
        return new WeatherProperties(new WeatherProperties.Cache(ttl), null, null);
    }

    /** A controllable provider that records how many times it was called. */
    private static final class FakeProvider implements WeatherProvider {
        private final String name;
        private final Weather result;
        private boolean healthy;
        private final AtomicInteger calls = new AtomicInteger();

        FakeProvider(String name, Weather result, boolean healthy) {
            this.name = name;
            this.result = result;
            this.healthy = healthy;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public Weather fetchCurrent(String city) {
            calls.incrementAndGet();
            if (!healthy) {
                throw new WeatherProviderException(name + " is down");
            }
            return result;
        }
    }

    @Test
    void returnsReadingFromPrimaryProvider() {
        var primary = new FakeProvider("primary", new Weather(29, 20), true);
        var secondary = new FakeProvider("secondary", new Weather(10, 5), true);
        var service = new WeatherService(List.of(primary, secondary), new WeatherCache(), props(Duration.ofSeconds(3)));

        Weather weather = service.getWeather("singapore");

        assertThat(weather).isEqualTo(new Weather(29, 20));
        assertThat(secondary.calls).hasValue(0);
    }

    @Test
    void failsOverToSecondaryWhenPrimaryIsDown() {
        var primary = new FakeProvider("primary", new Weather(29, 20), false);
        var secondary = new FakeProvider("secondary", new Weather(31, 12), true);
        var service = new WeatherService(List.of(primary, secondary), new WeatherCache(), props(Duration.ofSeconds(3)));

        Weather weather = service.getWeather("singapore");

        assertThat(weather).isEqualTo(new Weather(31, 12));
        assertThat(primary.calls).hasValue(1);
        assertThat(secondary.calls).hasValue(1);
    }

    @Test
    void servesFreshResultFromCacheWithoutHittingProviders() {
        var primary = new FakeProvider("primary", new Weather(29, 20), true);
        var service = new WeatherService(List.of(primary), new WeatherCache(), props(Duration.ofSeconds(3)));

        service.getWeather("singapore");
        service.getWeather("singapore");

        assertThat(primary.calls).hasValue(1);
    }

    @Test
    void servesStaleResultWhenAllProvidersAreDown() {
        var primary = new FakeProvider("primary", new Weather(29, 20), true);
        // ttl = 0 => the stored reading is never "fresh", forcing a provider refresh each call.
        var service = new WeatherService(List.of(primary), new WeatherCache(), props(Duration.ZERO));

        Weather first = service.getWeather("singapore");
        assertThat(first).isEqualTo(new Weather(29, 20));

        primary.healthy = false; // provider goes down after the cache was warmed
        Weather stale = service.getWeather("singapore");

        assertThat(stale).isEqualTo(new Weather(29, 20));
    }

    @Test
    void throwsWhenAllProvidersDownAndNoCacheExists() {
        var primary = new FakeProvider("primary", new Weather(29, 20), false);
        var service = new WeatherService(List.of(primary), new WeatherCache(), props(Duration.ofSeconds(3)));

        assertThatThrownBy(() -> service.getWeather("singapore"))
                .isInstanceOf(WeatherUnavailableException.class);
    }
}

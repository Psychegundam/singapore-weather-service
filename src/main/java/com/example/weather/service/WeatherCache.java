package com.example.weather.service;

import com.example.weather.domain.Weather;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A tiny, thread-safe, per-city cache of the last known weather reading.
 *
 * <p>Unlike a plain expiring cache, entries are never evicted on expiry. Once a
 * reading goes stale we keep it around so it can be served as a last resort when
 * every provider is down. Freshness is decided by the caller via {@link Entry#isFresh}.
 */
@Component
public class WeatherCache {

    private final ConcurrentHashMap<String, Entry> entries = new ConcurrentHashMap<>();

    public Optional<Entry> get(String city) {
        return Optional.ofNullable(entries.get(key(city)));
    }

    public void put(String city, Weather weather) {
        entries.put(key(city), new Entry(weather, Instant.now()));
    }

    private static String key(String city) {
        return city.trim().toLowerCase();
    }

    /**
     * A cached reading paired with the instant it was stored.
     */
    public record Entry(Weather weather, Instant storedAt) {

        public boolean isFresh(Duration ttl, Instant now) {
            return storedAt.plus(ttl).isAfter(now);
        }
    }
}

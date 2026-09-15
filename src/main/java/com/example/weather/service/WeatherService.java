package com.example.weather.service;

import com.example.weather.config.WeatherProperties;
import com.example.weather.domain.Weather;
import com.example.weather.provider.WeatherProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Orchestrates caching and provider failover.
 *
 * <p>Request flow:
 * <ol>
 *   <li>If a fresh cached reading exists (within the configured TTL), return it
 *       without touching any provider.</li>
 *   <li>Otherwise try each provider in {@code @Order} priority until one succeeds,
 *       cache the result, and return it.</li>
 *   <li>If every provider fails but a (stale) cached reading exists, serve it
 *       stale rather than erroring.</li>
 *   <li>Only if there is no reading at all do we surface a 503.</li>
 * </ol>
 *
 * <p>A per-city lock provides single-flight refresh: under load only one thread
 * refreshes a given city while others reuse the result, protecting the upstream
 * providers from a thundering herd.
 */
@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private final List<WeatherProvider> providers;
    private final WeatherCache cache;
    private final Duration ttl;
    private final ConcurrentHashMap<String, ReentrantLock> refreshLocks = new ConcurrentHashMap<>();

    public WeatherService(List<WeatherProvider> providers, WeatherCache cache, WeatherProperties properties) {
        this.providers = providers;
        this.cache = cache;
        this.ttl = properties.cache().ttl();
        log.info("Weather providers registered in failover order: {}",
                providers.stream().map(WeatherProvider::name).toList());
    }

    public Weather getWeather(String city) {
        Optional<WeatherCache.Entry> cached = cache.get(city);
        if (cached.isPresent() && cached.get().isFresh(ttl, Instant.now())) {
            return cached.get().weather();
        }
        return refresh(city);
    }

    private Weather refresh(String city) {
        ReentrantLock lock = refreshLocks.computeIfAbsent(city.trim().toLowerCase(), k -> new ReentrantLock());
        lock.lock();
        try {
            // Re-check under the lock: another thread may have just refreshed.
            Optional<WeatherCache.Entry> cached = cache.get(city);
            if (cached.isPresent() && cached.get().isFresh(ttl, Instant.now())) {
                return cached.get().weather();
            }

            for (WeatherProvider provider : providers) {
                try {
                    Weather weather = provider.fetchCurrent(city);
                    cache.put(city, weather);
                    log.debug("Served fresh reading for '{}' from {}", city, provider.name());
                    return weather;
                } catch (RuntimeException e) {
                    log.warn("Provider {} failed for '{}': {}", provider.name(), city, e.getMessage());
                }
            }

            // Every provider failed. Fall back to a stale reading if we have one.
            if (cached.isPresent()) {
                log.warn("All providers down for '{}'; serving stale reading from {}",
                        city, cached.get().storedAt());
                return cached.get().weather();
            }

            throw new WeatherUnavailableException(
                    "All weather providers are unavailable and no cached reading exists for '" + city + "'");
        } finally {
            lock.unlock();
        }
    }
}

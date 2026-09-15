package com.example.weather.provider;

/**
 * Thrown by a {@link WeatherProvider} when it cannot produce a reading.
 * Signals the orchestrating service to fail over to the next provider.
 */
public class WeatherProviderException extends RuntimeException {

    public WeatherProviderException(String message) {
        super(message);
    }

    public WeatherProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}

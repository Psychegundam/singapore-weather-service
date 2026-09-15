package com.example.weather.service;

/**
 * Thrown when no provider can supply a reading and no cached value (even a stale
 * one) exists to fall back on. Maps to HTTP 503 at the web layer.
 */
public class WeatherUnavailableException extends RuntimeException {

    public WeatherUnavailableException(String message) {
        super(message);
    }
}

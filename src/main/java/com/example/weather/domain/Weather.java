package com.example.weather.domain;

/**
 * Provider-agnostic weather snapshot.
 *
 * <p>All providers must normalize into this shape before it leaves the
 * provider layer, so the rest of the application never has to reason about
 * vendor-specific units or field names.
 *
 * @param temperatureDegreesCelsius air temperature in degrees Celsius
 * @param windSpeedKmh              wind speed in kilometres per hour
 */
public record Weather(double temperatureDegreesCelsius, double windSpeedKmh) {
}

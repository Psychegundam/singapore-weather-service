package com.example.weather.web;

import com.example.weather.service.WeatherUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates domain failures into RFC 7807 problem responses so clients get a
 * predictable, machine-readable error shape.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WeatherUnavailableException.class)
    public ProblemDetail handleUnavailable(WeatherUnavailableException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
        problem.setTitle("Weather temporarily unavailable");
        return problem;
    }
}

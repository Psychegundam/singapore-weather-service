package com.example.weather.web;

import com.example.weather.domain.Weather;
import com.example.weather.service.WeatherService;
import com.example.weather.service.WeatherUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherController.class)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @Test
    void returnsUnifiedJsonPayload() throws Exception {
        when(weatherService.getWeather(anyString())).thenReturn(new Weather(29.4, 20.2));

        mockMvc.perform(get("/v1/weather").param("city", "singapore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wind_speed").value(20))
                .andExpect(jsonPath("$.temperature_degrees").value(29));
    }

    @Test
    void returns503WhenWeatherUnavailable() throws Exception {
        when(weatherService.getWeather(anyString()))
                .thenThrow(new WeatherUnavailableException("all down"));

        mockMvc.perform(get("/v1/weather"))
                .andExpect(status().isServiceUnavailable());
    }
}

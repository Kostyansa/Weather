package org.example.weather.service;

import lombok.RequiredArgsConstructor;
import org.example.weather.entity.Town;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;
import tk.plogitech.darksky.forecast.APIKey;
import tk.plogitech.darksky.forecast.DarkSkyClient;
import tk.plogitech.darksky.forecast.ForecastRequestBuilder;
import tk.plogitech.darksky.forecast.model.DailyDataPoint;
import tk.plogitech.darksky.forecast.model.Forecast;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final DarkSkyClient darkSkyClient;

    private final APIKey apiKey;

    private ForecastRequestBuilder forecastRequestBuilder(){
        return new ForecastRequestBuilder();
    }

    public List<DailyDataPoint> getHistoryFor(Date start, Town town){
        return null;
    }

    public Forecast getForecast(Town town){
        return null;
    }


}

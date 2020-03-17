package org.example.weather.service;

import com.vk.api.sdk.objects.base.GeoCoordinates;
import lombok.RequiredArgsConstructor;
import org.example.weather.entity.Town;
import org.example.weather.repository.WeatherRepository;
import org.example.weather.service.mapper.GeoCoordinatesMapper;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;
import tk.plogitech.darksky.api.jackson.DarkSkyJacksonClient;
import tk.plogitech.darksky.forecast.*;
import tk.plogitech.darksky.forecast.model.DailyDataPoint;
import tk.plogitech.darksky.forecast.model.Forecast;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherService {


    private final DarkSkyJacksonClient darkSkyClient;
    private final GeoCoordinatesMapper geoCoordinatesMapper;

    private final WeatherRepository weatherRepository;

    private final APIKey apiKey;

    private ForecastRequestBuilder forecastRequestBuilder(){
        return new ForecastRequestBuilder();
    }

    public List<DailyDataPoint> getHistoryFor(Date start, Town town){
        return null;
    }

    public Forecast getForecast(Town town) throws ForecastException {
        ForecastRequestBuilder builder = new ForecastRequestBuilder();
        ForecastRequest request = forecastRequestBuilder()
                .key(apiKey)
                .time(LocalDateTime.now().toInstant(ZoneOffset.UTC))
                .location(town.getCoordinates())
                .language(ForecastRequestBuilder.Language.ru)
                .exclude(ForecastRequestBuilder.Block.alerts,
                        ForecastRequestBuilder.Block.minutely,
                        ForecastRequestBuilder.Block.flags)
                .units(ForecastRequestBuilder.Units.si)
                .build();
        return darkSkyClient.forecast(request);
    }

    public Forecast getForecast(GeoCoordinates geoCoordinates) throws ForecastException {
        ForecastRequestBuilder builder = new ForecastRequestBuilder();
        ForecastRequest request = forecastRequestBuilder()
                .key(apiKey)
                .time(LocalDateTime.now().toInstant(ZoneOffset.UTC))
                .location(geoCoordinatesMapper.toDarkSky(geoCoordinates))
                .language(ForecastRequestBuilder.Language.ru)
                .exclude(ForecastRequestBuilder.Block.alerts,
                        ForecastRequestBuilder.Block.minutely,
                        ForecastRequestBuilder.Block.flags)
                .units(ForecastRequestBuilder.Units.si)
                .build();
        return darkSkyClient.forecast(request);
    }
}

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
import tk.plogitech.darksky.forecast.model.Daily;
import tk.plogitech.darksky.forecast.model.DailyDataPoint;
import tk.plogitech.darksky.forecast.model.Forecast;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
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

    public Forecast getMostYear(Town town, String type, LocalDate start, LocalDate end){
        Forecast forecast = new Forecast();
        forecast.setDaily(new Daily());
        forecast.getDaily().setData(
                Collections.singletonList(weatherRepository.getTopOneInDatesInTownGroupByYearOrderByField(type, start, end, town))
        );
        return forecast;
    }

    public Forecast getMostMonth(Town town, String type, LocalDate start, LocalDate end){
        Forecast forecast = new Forecast();
        forecast.setDaily(new Daily());
        forecast.getDaily().setData(
                Collections.singletonList(weatherRepository.getTopOneInDatesInTownGroupByMonthOrderByField(type, start, end, town))
        );
        return forecast;
    }

    public Forecast getMostInDates(Town town, String type, LocalDate start, LocalDate end){
        Forecast forecast = new Forecast();
        forecast.setDaily(new Daily());
        forecast.getDaily().setData(
                Collections.singletonList(weatherRepository.getTopOneInDatesInTownOrderByField(type, start, end, town))
        );
        return forecast;
    }

    public Forecast getAverage(Town town, LocalDate start, LocalDate end){
        Forecast forecast = new Forecast();
        forecast.setDaily(new Daily());
        forecast.getDaily().setData(
                Collections.singletonList(weatherRepository.getAverageInDatesInTown(start, end, town))
        );
        return forecast;
    }
}

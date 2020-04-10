package org.example.weather.service;

import com.vk.api.sdk.objects.base.GeoCoordinates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.weather.entity.Town;
import org.example.weather.repository.TownRepository;
import org.example.weather.repository.WeatherRepository;
import org.example.weather.service.mapper.GeoCoordinatesMapper;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tk.plogitech.darksky.api.jackson.DarkSkyJacksonClient;
import tk.plogitech.darksky.forecast.*;
import tk.plogitech.darksky.forecast.model.Daily;
import tk.plogitech.darksky.forecast.model.DailyDataPoint;
import tk.plogitech.darksky.forecast.model.Forecast;

import java.time.Instant;
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
@Slf4j
public class WeatherService {


    private final DarkSkyJacksonClient darkSkyClient;
    private final GeoCoordinatesMapper geoCoordinatesMapper;

    private final WeatherRepository weatherRepository;

    private final TownService townService;

    private final APIKey apiKey;

    private ForecastRequestBuilder forecastRequestBuilder() {
        return new ForecastRequestBuilder();
    }

    public List<DailyDataPoint> getHistoryFor(Date start, Town town) {
        return null;
    }

    //@Scheduled(fixedDelay = 21600000)
    public void updateForecast() {
        List<Town> townList = townService.get();
        for (Town town : townList) {
            updateForecast(town);
        }
    }

    @Async
    private void updateForecast(Town town) {
        LocalDate localDate = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            Instant instant = localDate.plusDays(i).atStartOfDay().toInstant(ZoneOffset.UTC);
            try {
                weatherRepository.create(
                        getForecast(town, instant).getDaily().getData().get(0),
                        town
                );
            } catch (ForecastException exc) {
                log.debug("There was a problem with connecting to API", exc);
            } catch (NullPointerException | IndexOutOfBoundsException exc) {
                log.debug("Data for date: {} was not found", instant, exc);
            }
        }
    }

    public void addOldForecast(int days){
        List<Town> townList = townService.get();
        for (Town town : townList) {
            addOldForecast(town, days);
        }
    }

    @Async
    public void addOldForecast(Town town, int days){
        LocalDate from = weatherRepository.getMinDateForTown(town);
        if (from == null){
            from = LocalDate.now();
        }
        for (int i = 0; i < days; i++) {
            Instant instant = from.minusDays(i).atStartOfDay().toInstant(ZoneOffset.UTC);
            try {
                weatherRepository.create(
                        getForecast(town, instant).getDaily().getData().get(0),
                        town
                );
            } catch (ForecastException exc) {
                log.debug("There was a problem with connecting to API", exc);
                break;
            } catch (NullPointerException | IndexOutOfBoundsException exc) {
                log.debug("Data for date: {} was not found", instant, exc);
                break;
            } catch (Exception exc){
                log.debug("There was an error", exc);
            }
        }
    }

    public Forecast getForecast(Town town, Instant time) throws ForecastException {
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
                .time(time)
                .build();
        return darkSkyClient.forecast(request);
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

    public List<DailyDataPoint> getDailyDataPoint(Town town, LocalDate start, LocalDate end) {
        return weatherRepository.getInDatesInTown(start, end, town);
    }

    public Forecast getMostYear(Town town, String type, LocalDate start, LocalDate end) {
        Forecast forecast = new Forecast();
        forecast.setDaily(new Daily());
        forecast.getDaily().setData(
                Collections.singletonList(weatherRepository.getTopOneInDatesInTownGroupByYearOrderByField(type, start, end, town))
        );
        return forecast;
    }

    public Forecast getMostMonth(Town town, String type, LocalDate start, LocalDate end) {
        Forecast forecast = new Forecast();
        forecast.setDaily(new Daily());
        forecast.getDaily().setData(
                Collections.singletonList(weatherRepository.getTopOneInDatesInTownGroupByMonthOrderByField(type, start, end, town))
        );
        return forecast;
    }

    public Forecast getMostInDates(Town town, String type, LocalDate start, LocalDate end) {
        Forecast forecast = new Forecast();
        forecast.setDaily(new Daily());
        forecast.getDaily().setData(
                Collections.singletonList(weatherRepository.getTopOneInDatesInTownOrderByField(type, start, end, town))
        );
        return forecast;
    }

    public Forecast getAverage(Town town, LocalDate start, LocalDate end) {
        Forecast forecast = new Forecast();
        forecast.setDaily(new Daily());
        forecast.getDaily().setData(
                Collections.singletonList(weatherRepository.getAverageInDatesInTown(start, end, town))
        );
        return forecast;
    }
}

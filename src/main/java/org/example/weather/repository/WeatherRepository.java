package org.example.weather.repository;

import org.example.weather.entity.Town;
import tk.plogitech.darksky.forecast.model.DailyDataPoint;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;


public interface WeatherRepository {

    public DailyDataPoint get(LocalDate date);

    public List<DailyDataPoint> getInDatesInTown(LocalDate start, LocalDate end, Town town);

    DailyDataPoint getTopOneInDatesInTownOrderByField(String field, LocalDate start, LocalDate end, Town town);

    DailyDataPoint getTopOneInDatesInTownGroupByMonthOrderByField
            (String field, LocalDate start, LocalDate end, Town town);

    DailyDataPoint getTopOneInDatesInTownGroupByYearOrderByField
            (String field, LocalDate start, LocalDate end, Town town);

    DailyDataPoint getAverageInDatesInTown(LocalDate start, LocalDate end, Town town);

    public void create(DailyDataPoint dataPoint, Town town);
}

package org.example.weather.repository;

import org.example.weather.entity.Town;
import tk.plogitech.darksky.forecast.model.DailyDataPoint;

import java.util.Date;
import java.util.List;


public interface WeatherRepository {

    public DailyDataPoint get(Date date);

    public List<DailyDataPoint> getInDatesInTown(Date start, Date end, Town town);
}

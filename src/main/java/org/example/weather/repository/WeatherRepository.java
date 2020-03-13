package org.example.weather.repository;

import org.example.weather.entity.Town;
import org.springframework.stereotype.Repository;
import tk.plogitech.darksky.forecast.model.Forecast;

import java.util.Date;
import java.util.List;


public interface WeatherRepository {

    public Forecast get(Date date);

    public List<Forecast> getInDatesInTown(Date start, Date end, Town town);
}

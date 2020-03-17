package org.example.weather.repository.impl;

import lombok.RequiredArgsConstructor;
import org.example.weather.builder.DailyDataPointBuilder;
import org.example.weather.entity.Town;
import org.example.weather.repository.WeatherRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import tk.plogitech.darksky.forecast.model.DailyDataPoint;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class WeatherRepositoryImpl implements WeatherRepository {

    private final JdbcTemplate jdbcTemplate;

    private RowMapper<DailyDataPoint> rowMapper = (rowStr, rowNum) -> new DailyDataPointBuilder()
            .setTime(rowStr.getTimestamp("time").toInstant())
            .setApparentTemperatureHigh(rowStr.getDouble("apparentTemperatureHigh"))
            .setTemperatureHigh(rowStr.getDouble("temperatureHigh"))
            .setApparentTemperatureLow(rowStr.getDouble("apparentTemperatureLow"))
            .setTemperatureLow(rowStr.getDouble("temperatureLow"))
            .setCloudCover(rowStr.getDouble("cloudCover"))
            .setHumidity(rowStr.getDouble("humidity"))
            .setPressure(rowStr.getDouble("pressure"))
            .setPrecipProbability(rowStr.getDouble("precipProbability"))
            .setPrecipType(rowStr.getString("precipType"))
            .setPrecipIntensity(rowStr.getDouble("precipIntensity"))
            .setWindBearing(rowStr.getInt("windBearing"))
            .setWindSpeed(rowStr.getDouble("windSpeed"))
            .build();

    @Override
    public DailyDataPoint get(Date date) {
        try {
            return jdbcTemplate.queryForObject(
                    "select * from weather.forecast where time = ?;",
                    rowMapper,
                    Timestamp.from(date.toInstant())
            );
        }
        catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    @Override
    public List<DailyDataPoint> getInDatesInTown(Date start, Date end, Town town) {
        return jdbcTemplate.query(

                "select * from weather.forecast where time between ? and ?;",
                rowMapper,
                Timestamp.from(end.toInstant()),
                Timestamp.from(start.toInstant())
        );
    }

    @Override
    public void create(DailyDataPoint dataPoint, Town town) {
        jdbcTemplate.update(
                "insert into weather.forecast (" +
                        "time, " +
                        "summary, " +
                        "apparentTemperatureHigh, " +
                        "temperatureHigh, " +
                        "apparentTemperatureLow, " +
                        "temperatureLow, " +
                        "cloudCover, " +
                        "humidity, " +
                        "pressure, " +
                        "precipProbability, " +
                        "precipType, " +
                        "precipIntensity, " +
                        "windBearing, " +
                        "windSpeed, " +
                        "id_Town) " +
                        "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Instant.from(dataPoint.getTime()),
                dataPoint.getSummary(),
                dataPoint.getApparentTemperatureHigh(),
                dataPoint.getTemperatureHigh(),
                dataPoint.getApparentTemperatureLow(),
                dataPoint.getTemperatureLow(),
                dataPoint.getCloudCover(),
                dataPoint.getHumidity(),
                dataPoint.getPressure(),
                dataPoint.getPrecipProbability(),
                dataPoint.getPrecipType(),
                dataPoint.getPrecipIntensity(),
                dataPoint.getWindBearing(),
                dataPoint.getWindSpeed(),
                town.getId()
        );
    }
}

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
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class WeatherRepositoryImpl implements WeatherRepository {

    private final JdbcTemplate jdbcTemplate;

    private RowMapper<DailyDataPoint> rowMapper = (rowStr, rowNum) -> new DailyDataPointBuilder()
            .setTime(rowStr.getTimestamp(1).toInstant())
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

    private RowMapper<DailyDataPoint> groupRowMapper = (rowStr, rowNum) -> new DailyDataPointBuilder()
            .setTime(rowStr.getTimestamp("period").toInstant())
            .setApparentTemperatureHigh(rowStr.getDouble("apparentTemperatureHigh"))
            .setTemperatureHigh(rowStr.getDouble("temperatureHigh"))
            .setApparentTemperatureLow(rowStr.getDouble("apparentTemperatureLow"))
            .setTemperatureLow(rowStr.getDouble("temperatureLow"))
            .setCloudCover(rowStr.getDouble("cloudCover"))
            .setPressure(rowStr.getDouble("pressure"))
            .setPrecipIntensity(rowStr.getDouble("precipIntensity"))
            .setWindSpeed(rowStr.getDouble("windSpeed"))
            .build();

    @Override
    public DailyDataPoint get(LocalDate date) {
        try {
            return jdbcTemplate.queryForObject(
                    "select * from weather.forecast where time = ?;",
                    rowMapper,
                    Timestamp.from(Instant.from(date))
            );
        }
        catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    @Override
    public List<DailyDataPoint> getInDatesInTown(LocalDate start, LocalDate end, Town town) {
        return jdbcTemplate.query(

                "select * from weather.forecast where time between ? and ? and id_Town = ?;",
                rowMapper,
                Timestamp.from(Instant.from(start)),
                Timestamp.from(Instant.from(end)),
                town.getId()
        );
    }

    @Override
    public DailyDataPoint getTopOneInDatesInTownOrderByField(String field, LocalDate start, LocalDate end, Town town){
        try {
            return jdbcTemplate.queryForObject(
                    "select * from weather.forecast where time between ? and ? and id_Town = ? order by ? limit 1;",
                    rowMapper,
                    Timestamp.from(Instant.from(start)),
                    Timestamp.from(Instant.from(end)),
                    town.getId(),
                    field
            );
        }
        catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    @Override
    public DailyDataPoint getTopOneInDatesInTownGroupByMonthOrderByField
            (String field, LocalDate start, LocalDate end, Town town){
        try {
            return jdbcTemplate.queryForObject(
                    "select " +
                            "date_trunc('month', time) as period, " +
                            "avg(apparentTemperatureHigh), " +
                            "avg(temperatureHigh), " +
                            "avg(apparentTemperatureLow), " +
                            "avg(temperatureLow), " +
                            "avg(cloudCover), " +
                            "avg(pressure), " +
                            "avg(precipIntensity), " +
                            "avg(windSpeed) " +
                            "from weather.forecast " +
                            "where time between ? and ? " +
                            "and id_Town = ? " +
                            "group by period " +
                            "order by ? limit 1;",
                    groupRowMapper,
                    Timestamp.from(Instant.from(start.withDayOfMonth(1))),
                    Timestamp.from(Instant.from(end.withDayOfMonth(1))),
                    town.getId(),
                    field
            );
        }
        catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    @Override
    public DailyDataPoint getTopOneInDatesInTownGroupByYearOrderByField
            (String field, LocalDate start, LocalDate end, Town town){
        try {
            return jdbcTemplate.queryForObject(
                    "select " +
                            "date_trunc('year', time) as period, " +
                            "avg(apparentTemperatureHigh), " +
                            "avg(temperatureHigh), " +
                            "avg(apparentTemperatureLow), " +
                            "avg(temperatureLow), " +
                            "avg(cloudCover), " +
                            "avg(pressure), " +
                            "avg(precipIntensity), " +
                            "avg(windSpeed) " +
                            "from weather.forecast " +
                            "where time between ? and ? " +
                            "and id_Town = ? " +
                            "group by period " +
                            "order by ? limit 1;",
                    groupRowMapper,
                    Timestamp.from(Instant.from(start.withDayOfYear(1))),
                    Timestamp.from(Instant.from(end.withDayOfYear(1))),
                    town.getId(),
                    field
            );
        }
        catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    @Override
    public DailyDataPoint getAverageInDatesInTown(LocalDate start, LocalDate end, Town town){
        try {
            return jdbcTemplate.queryForObject(
                    "select " +
                            "min(time) as period, " +
                            "avg(apparentTemperatureHigh), " +
                            "avg(temperatureHigh), " +
                            "avg(apparentTemperatureLow), " +
                            "avg(temperatureLow), " +
                            "avg(cloudCover), " +
                            "avg(pressure), " +
                            "avg(precipIntensity), " +
                            "avg(windSpeed) " +
                            "from weather.forecast " +
                            "where time between ? and ? " +
                            "and id_Town = ?;",
                    groupRowMapper,
                    Timestamp.from(Instant.from(start.withDayOfMonth(1))),
                    Timestamp.from(Instant.from(end.withDayOfMonth(1))),
                    town.getId()
            );
        }
        catch (EmptyResultDataAccessException e){
            return null;
        }
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

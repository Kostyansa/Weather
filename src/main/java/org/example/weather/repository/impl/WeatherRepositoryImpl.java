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
import java.time.LocalDateTime;
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
            .setSummary(rowStr.getString("summary"))
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
    public List<DailyDataPoint> getInDatesInTown(LocalDate start, LocalDate end, Town town) {
        return jdbcTemplate.query(

                "select * from weather.forecast where time between ? and ? and id_Town = ?;",
                rowMapper,
                Timestamp.valueOf(start.atStartOfDay()),
                Timestamp.valueOf(end.atStartOfDay()),
                town.getId()
        );
    }

    @Override
    public LocalDate getMinDateForTown(Town town){
        try {
            return jdbcTemplate.queryForObject(
                    "select min(time) from weather.forecast where id_Town = ?;",
                    LocalDate.class,
                    town.getId()
            );
        }
        catch (EmptyResultDataAccessException exc){
            return null;
        }
    }

    @Override
    public DailyDataPoint getTopOneInDatesInTownOrderByField(String field, LocalDate start, LocalDate end, Town town){
        try {
            return jdbcTemplate.queryForObject(
                    String.format("select * from weather.forecast where time between ? and ? and id_Town = ? order by %s limit 1;", field),
                    rowMapper,
                    Timestamp.valueOf(start.atStartOfDay()),
                    Timestamp.valueOf(end.atStartOfDay()),
                    town.getId()
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
                    String.format("select " +
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
                            "order by %s limit 1;", field),
                    groupRowMapper,
                    Timestamp.valueOf(start.withDayOfMonth(1).atStartOfDay()),
                    Timestamp.valueOf(end.withDayOfMonth(1).atStartOfDay()),
                    town.getId()
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
                    String.format("select " +
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
                            "order by %s limit 1;", field),
                    groupRowMapper,
                    Timestamp.valueOf(start.withDayOfYear(1).atStartOfDay()),
                    Timestamp.valueOf(end.withDayOfYear(1).atStartOfDay()),
                    town.getId()
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
                    Timestamp.valueOf(start.withDayOfMonth(1).atStartOfDay()),
                    Timestamp.valueOf(end.withDayOfMonth(1).atStartOfDay()),
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
                        "values" +
                        "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                        "on conflict(id_town, time) do update " +
                        "set " +
                        "summary = excluded.summary, " +
                        "apparentTemperatureHigh  = excluded.apparentTemperatureHigh, " +
                        "temperatureHigh = excluded.temperatureHigh, " +
                        "apparentTemperatureLow = excluded.apparentTemperatureLow, " +
                        "temperatureLow = excluded.temperatureLow, " +
                        "cloudCover = excluded.cloudCover, " +
                        "humidity = excluded.humidity, " +
                        "pressure = excluded.pressure, " +
                        "precipProbability = excluded.precipProbability, " +
                        "precipType = excluded.precipType, " +
                        "precipIntensity = excluded.precipIntensity, " +
                        "windBearing = excluded.windBearing, " +
                        "windSpeed = excluded.windSpeed;",
                Timestamp.from(dataPoint.getTime()),
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

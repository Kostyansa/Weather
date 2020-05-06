package org.example.weather.repository.impl;

import lombok.RequiredArgsConstructor;
import org.example.weather.entity.Town;
import org.example.weather.repository.TownRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import tk.plogitech.darksky.forecast.GeoCoordinates;
import tk.plogitech.darksky.forecast.model.Latitude;
import tk.plogitech.darksky.forecast.model.Longitude;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TownRepositoryImpl implements TownRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Town> rowMapper = (rowStr, rowNum) -> new Town(
            rowStr.getLong("id"),
            rowStr.getString("name"),
            new GeoCoordinates(
                    new Longitude(rowStr.getDouble("longitude")),
                    new Latitude(rowStr.getDouble("latitude")))
    );

    @Override
    public Town get(Integer id) {
        try {
            return jdbcTemplate.queryForObject(
                    "select id, name, longitude, latitude from weather.town where id = ?;",
                    rowMapper,
                    id
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Town> get() {
        return jdbcTemplate.query(
                "select id, name, longitude, latitude from weather.town;",
                rowMapper
        );
    }


    @Override
    public Town getByName(String name) {

        try {
            return jdbcTemplate.queryForObject(
                    "select id, name, longitude, latitude from weather.town where name = ?;",
                    rowMapper,
                    name
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}

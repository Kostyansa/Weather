package org.example.weather.repository.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.MethodNotSupportedException;
import org.example.weather.entity.Town;
import org.example.weather.entity.User;
import org.example.weather.repository.UserRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import tk.plogitech.darksky.forecast.GeoCoordinates;
import tk.plogitech.darksky.forecast.model.Latitude;
import tk.plogitech.darksky.forecast.model.Longitude;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private RowMapper<User> rowMapper = (rowStr, rowNum) -> new User(
            rowStr.getLong("id"),
            new Town(
                    rowStr.getLong("id_town"),
                    rowStr.getString("town_name"),
                            new GeoCoordinates(
                                    new Longitude(rowStr.getDouble("town_longitude")),
                                    new Latitude(rowStr.getDouble("town_latitude"))
                            )
            )
    );

    @Override
    public User get(Integer id) {
        try {
            return jdbcTemplate.queryForObject(
                "select u.id as \"id\", " +
                        "t.id as \"id_town\", " +
                        "t.name as \"town_name\", " +
                        "t.longitude as \"town_longitude\", " +
                        "t.latitude as \"town_latitude\" " +
                        "from weather.user as u left join weather.town as t on " +
                        "u.id_town = t.id where u.id = ?;",
                rowMapper,
                id
            );
        }
        catch (EmptyResultDataAccessException e){
            log.debug("User with id {} was not found", id);
            return null;
        }
    }

    @Override
    public void create(Integer id) {
        jdbcTemplate.update("insert into weather.user (id, id_town) " +
                "values(?, NULL)", id);
    }

    @Override
    public void update(User user) {
        jdbcTemplate.update(
                "update weather.user set id_town = ? where id = ?",
                user.getTown().getId(),
                user.getId()
        );
    }
}

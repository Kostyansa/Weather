package org.example.weather.repository.impl;

import lombok.RequiredArgsConstructor;
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
public class UserRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private RowMapper<User> rowMapper = (rowStr, rowNum) -> new User(
            rowStr.getLong("id"),
            new Town(
                    rowStr.getLong("town_id"),
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
                        "t.id as \"town_id\", " +
                        "t.name as \"town_name\", " +
                        "t.longitude as \"town_longitude\" " +
                        "t.latitude as \"town_latitude\" " +
                        "from weather.\"user\" as u inner join weather.town as t on " +
                        "u.town_id = t.id where id = ?;",
                rowMapper,
                id
            );
        }
        catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    @Override
    public void create() {
        jdbcTemplate.update("insert into weather.user (town_id) " +
                "values(NULL)");
    }

    @Override
    public void update(User user) {
        jdbcTemplate.update(
                "update weather.user set town_id = ? where id = ?",
                user.getTown().getId(),
                user.getId()
        );
    }
}

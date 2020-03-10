package org.example.weather.repository;

import lombok.RequiredArgsConstructor;
import org.example.weather.entity.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public Optional<User> get(Integer id) {
        return Optional.empty();
    }
}

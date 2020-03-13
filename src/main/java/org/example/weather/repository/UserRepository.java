package org.example.weather.repository;

import lombok.RequiredArgsConstructor;
import org.example.weather.entity.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface UserRepository {

    public Optional<User> get(Integer id);

    public User create(Integer id);

    public void update(User user);
}

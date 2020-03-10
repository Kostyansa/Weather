package org.example.weather.service;

import org.example.weather.entity.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    public Optional<User> get(Integer id){
        return Optional.empty();
    }

    public User create(Integer id){
        return null;
    }

    public void update(User user){
    }
}

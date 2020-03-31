package org.example.weather.service;

import lombok.RequiredArgsConstructor;
import org.example.weather.entity.Town;
import org.example.weather.entity.User;
import org.example.weather.repository.UserRepository;
import org.springframework.stereotype.Service;
import tk.plogitech.darksky.forecast.GeoCoordinates;
import tk.plogitech.darksky.forecast.model.Latitude;
import tk.plogitech.darksky.forecast.model.Longitude;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User get(Integer id){
        return userRepository.get(id);
    }

    public User create(Integer id){
        userRepository.create(id);
        return new User( (long) id, null);
    }

    public void update(User user){
        userRepository.update(user);
    }
}

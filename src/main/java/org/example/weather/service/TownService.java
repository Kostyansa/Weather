package org.example.weather.service;

import lombok.RequiredArgsConstructor;
import org.example.weather.entity.Town;
import org.example.weather.repository.TownRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TownService {

    private final TownRepository townRepository;

    Town get(String town){
        return null;
    }
}

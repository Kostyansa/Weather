package org.example.weather.service;

import lombok.RequiredArgsConstructor;
import org.example.weather.entity.Town;
import org.example.weather.repository.TownRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TownService {

    private final TownRepository townRepository;

    public List<Town> get(){
        return townRepository.get();
    }

    public Town get(String town){
        return townRepository.getByName(town);
    }
}

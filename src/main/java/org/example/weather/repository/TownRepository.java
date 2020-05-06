package org.example.weather.repository;

import org.example.weather.entity.Town;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;


public interface TownRepository {

    public Town get(Integer id);

    List<Town> get();

    public Town getByName(String name);
}

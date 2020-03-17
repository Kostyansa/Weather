package org.example.weather.repository;

import org.example.weather.entity.Town;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;


public interface TownRepository {

    public Town get(Integer id);

    public Town getByName(String name);
}

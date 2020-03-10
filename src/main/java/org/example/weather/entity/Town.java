package org.example.weather.entity;

import lombok.Data;
import tk.plogitech.darksky.forecast.GeoCoordinates;

import java.math.BigDecimal;

@Data
public class Town {

    private Long id;
    private Long name;

    GeoCoordinates coordinates;

}

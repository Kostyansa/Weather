package org.example.weather.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tk.plogitech.darksky.forecast.GeoCoordinates;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Town {

    private Long id;
    private String name;

    GeoCoordinates coordinates;

}

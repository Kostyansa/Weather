package org.example.weather.service.mapper;

import org.springframework.stereotype.Service;
import tk.plogitech.darksky.forecast.GeoCoordinates;
import tk.plogitech.darksky.forecast.model.Latitude;
import tk.plogitech.darksky.forecast.model.Longitude;

@Service
public class GeoCoordinatesMapper {

    public GeoCoordinates toDarkSky(com.vk.api.sdk.objects.base.GeoCoordinates coordinates){
        return new GeoCoordinates(
                new Longitude((double) coordinates.getLongitude()),
                new Latitude((double) coordinates.getLatitude()));
    }
}

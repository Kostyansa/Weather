package org.example.weather.builder;

import tk.plogitech.darksky.forecast.model.DailyDataPoint;

import java.time.Instant;

public class DailyDataPointBuilder {

    private DailyDataPoint dataPoint;

    public DailyDataPointBuilder() {
        this.dataPoint = new DailyDataPoint();
    }

    public DailyDataPoint build(){
        return this.dataPoint;
    }

    public DailyDataPointBuilder setTime(Instant instant){
        dataPoint.setTime(instant);
        return this;
    }

    public DailyDataPointBuilder setSummary(String summary){
        dataPoint.setSummary(summary);
        return this;
    }

    public DailyDataPointBuilder setApparentTemperatureHigh(Double apparentTemperatureHigh){
        dataPoint.setApparentTemperatureHigh(apparentTemperatureHigh);
        return this;
    }

    public DailyDataPointBuilder setTemperatureHigh(Double temperatureHigh){
        dataPoint.setTemperatureHigh(temperatureHigh);
        return this;
    }

    public DailyDataPointBuilder setApparentTemperatureLow(Double apparentTemperatureLow){
        dataPoint.setApparentTemperatureLow(apparentTemperatureLow);
        return this;
    }

    public DailyDataPointBuilder setTemperatureLow(Double temperatureLow){
        dataPoint.setTemperatureLow(temperatureLow);
        return this;
    }

    public DailyDataPointBuilder setCloudCover(Double cloudCover){
        dataPoint.setCloudCover(cloudCover);
        return this;
    }

    public DailyDataPointBuilder setHumidity(Double humidity){
        dataPoint.setHumidity(humidity);
        return this;
    }

    public DailyDataPointBuilder setPressure(Double pressure){
        dataPoint.setPressure(pressure);
        return this;
    }

    public DailyDataPointBuilder setPrecipProbability(Double precipProbability){
        dataPoint.setPrecipProbability(precipProbability);
        return this;
    }

    public DailyDataPointBuilder setPrecipType(String precipType){
        dataPoint.setPrecipType(precipType);
        return this;
    }

    public DailyDataPointBuilder setPrecipIntensity(Double precipIntensity){
        dataPoint.setPrecipIntensity(precipIntensity);
        return this;
    }

    public DailyDataPointBuilder setWindBearing(Integer windBearing){
        dataPoint.setWindBearing(windBearing);
        return this;
    }

    public DailyDataPointBuilder setWindSpeed(Double windSpeed){
        dataPoint.setWindSpeed(windSpeed);
        return this;
    }
}

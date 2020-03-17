package org.example.weather.configuration;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tk.plogitech.darksky.api.jackson.DarkSkyJacksonClient;
import tk.plogitech.darksky.forecast.APIKey;

@Configuration
public class DarkSkyConfiguration {

    @Bean
    public DarkSkyJacksonClient darkSkyClient(){
        return new DarkSkyJacksonClient();
    }

    @Bean
    public APIKey darkSkyApiKey(@Value("${darkSky.key}") String apiKey){
        return new APIKey(apiKey);
    }
}

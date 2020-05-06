package org.example.weather.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

@Configuration
public class FormatterConfiguration {

    @Bean
    public DateTimeFormatter dateTimeFormatter(){
        return DateTimeFormatter.ofPattern("dd.MM.yyyy");
    }
}

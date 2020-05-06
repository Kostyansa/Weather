package org.example.weather.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Request {

    String command;
    String townName;
    LocalDate start;
    LocalDate end;
    String[] split;

}

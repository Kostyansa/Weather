package org.example.weather.service;

import com.vk.api.sdk.objects.base.Geo;
import com.vk.api.sdk.objects.base.GeoCoordinates;
import com.vk.api.sdk.objects.messages.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.weather.entity.Request;
import org.example.weather.entity.Town;
import org.example.weather.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tk.plogitech.darksky.forecast.ForecastException;
import tk.plogitech.darksky.forecast.model.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommandService {

    private final UserService userService;
    private final TownService townService;
    private final WeatherService weatherService;

    public String execute(Message message) {
        log.trace("Received message: {} from {}", message.getText(), message.getFromId());
        User user = userService.get(message.getFromId());
        if (user == null) {
            user = userService.create(message.getFromId());
            log.info("Created new User with id:{}", user.getId());
        }
        return route(
                message,
                user
        );
    }

    public String route(Message message, User user) {
        String[] split = message.getText().split(" ");
        Request request = parseRequest(split);
        switch (request.getCommand().toLowerCase()) {
            case "установить": {
                return updateUser(request, user);
            }
            case "погода":
            case "прогноз": {
                return forecast(request, user);
            }
            case "самый": {
                return statistics(request, user);
            }
            default:
                return "Команда не распознана";
        }
    }

    private Request parseRequest(String[] split) {
        Request request = new Request();
        request.setSplit(split);
        parseCommand(split, request);
        parseTownName(split, request);
        parseDates(split, request);
        return request;
    }

    private Request parseTownName(String[] split, Request request) {
        Integer townIndex = null;
        for (int i = 1; i < split.length - 1; i++) {
            if (split[i].equalsIgnoreCase("Город")) {
                townIndex = i;
                break;
            }
        }
        if (townIndex == null) {
            request.setTownName("");
        }
        StringBuilder townName = new StringBuilder();
        for (String s : split) {
            if (s.equalsIgnoreCase("Дата")) {
                break;
            }
            townName.append(s.toLowerCase()).append(" ");
        }
        request.setTownName(townName.toString());
        return request;
    }

    private Request parseCommand(String[] split, Request request) {
        if (split.length == 0) {
            request.setCommand("invalid");
        }
        request.setCommand(split[0]);
        return request;
    }

    private Request parseDates(String[] split, Request request) {
        Integer dataIndex = null;
        for (int i = 1; i < split.length - 1; i++) {
            if (split[i].equalsIgnoreCase("Дата")) {
                dataIndex = i;
                break;
            }
        }
        if (dataIndex == null) {
            return request;
        }
        if (dataIndex + 1 < split.length) {
            request.setStart(LocalDate.parse(split[dataIndex + 1]));
        }
        if (dataIndex + 2 < split.length) {
            request.setEnd(LocalDate.parse(split[dataIndex + 2]));
        }
        return request;
    }

    private String updateUser(Request request, User user) {
        String townName = request.getTownName();
        Town town = townService.get(townName);
        if (town == null) {
            return "Город не найден";
        }
        user.setTown(town);
        userService.update(user);
        return "Город успешно установлен";
    }

    private String forecast(Request request, User user) {
        Town town = townService.get(request.getTownName());
        if (town == null){
            town = user.getTown();
        }
        try {
            if (town == null) {
                if (request.getTownName().equals("")) {
                    return "Пожалуйста укажите город";
                }
                else {
                    return "Город не найден";
                }
            } else {
                return "В городе " +
                        town.getName() +
                        forecastFormat(weatherService.getForecast(town));
            }
        } catch (ForecastException e) {
            return "Произошла ошибка на стороне сервера";
        }
    }

    private String statistics(Request request, User user) {
        return "В разработке";
    }

    private String forecastFormat(Forecast forecast){
        StringBuilder stringBuilder = new StringBuilder();
        Daily daily = forecast.getDaily();
        for (DailyDataPoint dataPoint : daily.getData()){
            stringBuilder.append("Прогноз на ")
                    .append(LocalDate.ofInstant(dataPoint.getTime(), ZoneId.systemDefault()))
                    .append(dataPoint.getTemperatureHigh());
        }
        return stringBuilder.toString();
    }
}

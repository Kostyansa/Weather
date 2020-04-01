package org.example.weather.service;

import com.vk.api.sdk.objects.messages.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.weather.entity.Request;
import org.example.weather.entity.Town;
import org.example.weather.entity.User;
import org.springframework.stereotype.Service;
import tk.plogitech.darksky.forecast.ForecastException;
import tk.plogitech.darksky.forecast.model.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
                if (split.length < 3) {
                    return "Неправильный формат команды";
                }
                return theMost(request, user);
            }
            case "статистика": {
                if (split.length < 3) {
                    return "Неправильный формат команды";
                }
                return average(request, user);
            }
            case "updateforecast": {
                try {
                    weatherService.updateForecast();
                    return "Updated";
                } catch (Exception exc) {
                    return exc.getMessage();
                }
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
                townIndex = i + 1;
                break;
            }
        }
        if (townIndex == null) {
            request.setTownName("");
            return request;
        }
        StringBuilder townName = new StringBuilder();
        for (int i = townIndex; i < split.length; i++) {
            if (split[i].equalsIgnoreCase("Дата")) {
                break;
            }
            townName.append(split[i].toLowerCase()).append(" ");
        }
        townName.delete(townName.length() - 1, townName.length());
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

    private ChronoUnit exctractTimeUnit(String string) {
        switch (string) {
            case "День": {
                return ChronoUnit.DAYS;
            }
            case "Месяц": {
                return ChronoUnit.MONTHS;
            }
            case "Год": {
                return ChronoUnit.YEARS;
            }
            default: {
                return null;
            }
        }

    }

    private String updateUser(Request request, User user) {
        String townName = request.getTownName();
        Town town = townService.get(townName);
        if (town == null) {
            log.debug("Town name: {}", townName);
            return "Город не найден";
        }
        user.setTown(town);
        userService.update(user);
        return "Город успешно установлен";
    }

    private String forecast(Request request, User user) {
        Town town = townService.get(request.getTownName());
        if (town == null) {
            town = user.getTown();
        }
        if (town == null) {
            return "Пожалуйста укажите город";
        } else {
            return "В городе " +
                    town.getName() +
                    forecastFormat(weatherService.getDailyDataPoint(town, LocalDate.now(), LocalDate.now().plusDays(7)));
        }
    }

    private String theMost(Request request, User user) {
        String typeString = request.getSplit()[1];
        ChronoUnit time = exctractTimeUnit(request.getSplit()[2]);
        Town town = townService.get(request.getTownName());
        if (request.getEnd() == null) {
            return "Не указан промежуток";
        }
        String type = "";
        if (town == null) {
            town = user.getTown();
        }
        switch (typeString) {
            case "Холодный": {
                type = "TemperatureLow";
                break;
            }
            case "Жаркий": {
                type = "TemperatureHigh";
                break;
            }
            case "Ветреный": {
                type = "WindSpeed";
                break;
            }
            default: {
                return "Неправильный формат команды";
            }
        }
        Forecast forecast = null;
        switch (time) {
            case DAYS: {
                forecast = weatherService.getMostInDates(
                        town,
                        type,
                        request.getStart(),
                        request.getEnd());
                break;
            }
            case MONTHS: {
                forecast = weatherService.getMostMonth(
                        town,
                        type,
                        request.getStart(),
                        request.getEnd());
                break;
            }
            case YEARS: {
                forecast = weatherService.getMostYear(
                        town,
                        type,
                        request.getStart(),
                        request.getEnd());
                break;
            }
            default: {
                return "Не указан промежуток";
            }
        }
        return mostFormat(forecast, time, request.getStart(), request.getEnd());
    }

    private String average(Request request, User user) {
        Town town = townService.get(request.getTownName());
        if (town == null) {
            town = user.getTown();
        }
        if (town == null) {
            return "Пожалуйста укажите город";
        }
        if (request.getEnd() == null) {
            return "Не указан промежуток";
        }
        return averageFormat(
                weatherService.getAverage(town, request.getStart(), request.getEnd()),
                request.getStart(),
                request.getEnd());
    }

    private String forecastFormat(List<DailyDataPoint> dataPoints) {
        StringBuilder stringBuilder = new StringBuilder();
        for (DailyDataPoint dataPoint : dataPoints) {
            stringBuilder.append("Прогноз на ")
                    .append(LocalDate.ofInstant(dataPoint.getTime(), ZoneId.systemDefault()))
                    .append(dataPoint.getSummary())
                    .append('\n');
        }
        return stringBuilder.toString();
    }

    private String mostFormat(Forecast forecast, ChronoUnit chronoUnit, LocalDate start, LocalDate end) {
        StringBuilder stringBuilder = new StringBuilder();
        Daily daily = forecast.getDaily();
        for (DailyDataPoint dataPoint : daily.getData()) {
            stringBuilder.append("Прогноз на ")
                    .append(LocalDate.ofInstant(dataPoint.getTime(), ZoneId.systemDefault()))
                    .append(dataPoint.getSummary());
        }
        return stringBuilder.toString();
    }

    private String averageFormat(Forecast forecast, LocalDate start, LocalDate end) {
        StringBuilder stringBuilder = new StringBuilder();
        Daily daily = forecast.getDaily();
        for (DailyDataPoint dataPoint : daily.getData()) {
            stringBuilder.append("Прогноз на ")
                    .append(LocalDate.ofInstant(dataPoint.getTime(), ZoneId.systemDefault()))
                    .append(dataPoint.getSummary());
        }
        return stringBuilder.toString();
    }
}

package org.example.weather.service;

import com.vk.api.sdk.objects.messages.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.weather.entity.Request;
import org.example.weather.entity.Town;
import org.example.weather.entity.User;
import org.springframework.stereotype.Service;
import tk.plogitech.darksky.forecast.model.*;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommandService {

    private final UserService userService;
    private final TownService townService;
    private final WeatherService weatherService;
    private final DateTimeFormatter formatter;
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private final String[] bearings = {
            "Северный",
            "Северо-восточный",
            "Восточный",
            "Юго-восточный",
            "Южный",
            "Юго-западный",
            "Западный",
            "Северо-Западный",
            "Северный"
    };

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
        Request request;
        try {
            request = parseRequest(split);
        } catch (ParseException exc) {
            return "Неправильный формат даты";
        }
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
            case "список": {
                return townService.get().stream().
                        map(town -> {
                            return town.getName() + " - " + formatter.format(weatherService.getMinDateFor(town));
                        }).
                        reduce((identity, str) -> {
                            return identity.concat("\n").concat(str);
                        }).
                        orElseGet(() -> "Список городов пуст");
            }
            case "помощь": {
                return "Список команд:\n" +
                        "Список городов - выводит список городов для которых доступен прогноз " +
                        "и самую раннюю дату для которой есть данные\n\n" +
                        "Установить город <название> - устнавливает город по умолчанию для пользователя\n\n" +
                        "Прогноз город <название> - выводит прогноз на неделю в указанном городе" +
                        "если город не указан - в городе по умолчанию\n\n" +
                        "Самый <холодный/жаркий/ветренный> <день/месяц/год> город <название> Дата <начало> <конец> - " +
                        "выводит самый холодный/жаркий/ветренный день/месяц/год в городе, даты указываются в формате " +
                        "dd.MM.yyyy, конечная дата не учитывается, даты всегда указываются в полном формате\n\n" +
                        "Статистика город <название> Дата <начало> <конец> - выводит средние показатели температуры, " +
                        "скорости ветра, количества осадков и давления за промежуток времени, конечная дата не учитывается\n";
            }
            case "updateforecast": {
                try {
                    weatherService.updateForecast();
                    return "Updated";
                } catch (Exception exc) {
                    return exc.getMessage();
                }
            }
            case "oldforecast": {
                try {
                    if (split.length < 3) {
                        weatherService.addOldForecast(Integer.parseInt(request.getSplit()[1]));
                    }
                    else{
                        weatherService.addOldForecast(townService.get(request.getSplit()[2]),Integer.parseInt(request.getSplit()[1]));
                    }
                    return "Success";
                } catch (Exception exc) {
                    return exc.getMessage();
                }
            }
            default:
                return "Команда не распознана";
        }
    }

    private Request parseRequest(String[] split) throws ParseException {
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

    private Request parseDates(String[] split, Request request) throws ParseException {
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
            request.setStart(LocalDate.from(formatter.parse(split[dataIndex + 1])));
        }
        if (dataIndex + 2 < split.length) {
            request.setEnd(LocalDate.from(formatter.parse(split[dataIndex + 2])));
        }
        return request;
    }

    private ChronoUnit extractTimeUnit(String string) {
        switch (string.toLowerCase()) {
            case "день": {
                return ChronoUnit.DAYS;
            }
            case "месяц": {
                return ChronoUnit.MONTHS;
            }
            case "год": {
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
        String timeString = request.getSplit()[2];
        ChronoUnit time = extractTimeUnit(timeString);
        Town town = townService.get(request.getTownName());
        if (request.getEnd() == null) {
            return "Не указан промежуток";
        }
        String type = "";
        if (town == null) {
            town = user.getTown();
        }
        switch (typeString.toLowerCase()) {
            case "холодный": {
                type = "TemperatureLow asc";
                break;
            }
            case "жаркий": {
                type = "TemperatureHigh desc";
                break;
            }
            case "ветреный": {
                type = "WindSpeed desc";
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
        return mostFormat(forecast, timeString, typeString, request.getStart(), request.getEnd());
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
            stringBuilder.append(String.format(
                    " Прогноз на %s:\n" +
                            "%s\n" +
                            "Температура от %s до %s C\n" +
                            "Ощущается как от %s до %s C\n" +
                            "Скорость ветра: %s м/с\n",
                            "Давление: %s КПа\n",
                    formatter.format(LocalDate.ofInstant(dataPoint.getTime(), ZoneId.systemDefault())),
                    dataPoint.getSummary(),
                    dataPoint.getTemperatureLow(),
                    dataPoint.getTemperatureHigh(),
                    dataPoint.getApparentTemperatureLow(),
                    dataPoint.getApparentTemperatureHigh(),
                    dataPoint.getWindSpeed(),
                    dataPoint.getPressure()));
            int bearing = dataPoint.getWindBearing() / 45;
            stringBuilder
                    .append(bearings[bearing])
                    .append(", градусах: ")
                    .append(dataPoint.getWindBearing())
                    .append("\n");
            if (dataPoint.getPrecipProbability() == null){
                stringBuilder.append("Осадков не ожидается\n");
            }
            else {
                stringBuilder.append("Осадки: ")
                        .append(dataPoint.getPrecipIntensity())
                        .append("мм/ч");
            }
            stringBuilder.append("\n\n");
        }
        return stringBuilder.toString();
    }

    private String mostFormat(Forecast forecast, String period, String type, LocalDate start, LocalDate end) {
        StringBuilder stringBuilder = new StringBuilder();
        Daily daily = forecast.getDaily();
        for (DailyDataPoint dataPoint : daily.getData()) {
            stringBuilder.append(String.format(
                    "Самый %s %s на %s : %s:\n",
                    type,
                    period,
                    formatter.format(LocalDate.ofInstant(dataPoint.getTime(), ZoneId.systemDefault())),
                    formatter.format(end)
            ))
                    .append(formatter.format(LocalDate.ofInstant(dataPoint.getTime(), ZoneId.systemDefault()))).append("\n");
            switch (type.toLowerCase()) {
                case "холодный": {
                    stringBuilder.append(decimalFormat.format(dataPoint.getTemperatureLow()));
                    break;
                }
                case "жаркий": {
                    stringBuilder.append(decimalFormat.format(dataPoint.getTemperatureHigh()));
                    break;
                }
                case "ветреный": {
                    stringBuilder.append(decimalFormat.format(dataPoint.getWindSpeed()));
                    break;
                }
            }
        }
        return stringBuilder.toString();
    }

    private String averageFormat(Forecast forecast, LocalDate start, LocalDate end) {
        StringBuilder stringBuilder = new StringBuilder();
        List<DailyDataPoint> dataPoints = forecast.getDaily().getData();
        for (DailyDataPoint dataPoint : dataPoints) {
            stringBuilder.append(String.format(
                    "Средние показатели на %s : %s:\n" +
                            "Температура от %s до %s C\n" +
                            "Скорость ветра: %s м/с\n" +
                            "Осадки: %s мм/ч\n" +
                            "Давление: %s КПа\n",
                    formatter.format(LocalDate.ofInstant(dataPoint.getTime(), ZoneId.systemDefault())),
                    formatter.format(end),
                    decimalFormat.format(dataPoint.getTemperatureLow()),
                    decimalFormat.format(dataPoint.getTemperatureHigh()),
                    decimalFormat.format(dataPoint.getWindSpeed()),
                    decimalFormat.format(dataPoint.getPrecipIntensity()),
                    decimalFormat.format(dataPoint.getPressure()/10)));
        }
        return stringBuilder.toString();
    }
}

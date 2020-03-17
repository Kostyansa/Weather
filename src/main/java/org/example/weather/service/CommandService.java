package org.example.weather.service;

import com.vk.api.sdk.objects.messages.Message;
import lombok.RequiredArgsConstructor;
import org.example.weather.entity.Town;
import org.example.weather.entity.User;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CommandService {

    private final UserService userService;
    private final TownService townService;

    public String execute(Message message){
        Optional<User> user = userService.get(message.getFromId());
        return route(
                message.getText(),
                user.orElseGet(() -> userService.create(message.getFromId()))
        );
    }

    public String route(String command, User user){
        String[] split = command.split(" ");
        switch (split[0]){
            case "установить": {
                String townName = parseTownName(split);
                Town town = townService.get(townName);
                return updateUser(town, user);
            }
            default:
                return "Команда не распознана";
        }
    }

    private String parseTownName(String[] split){
        StringBuilder town = new StringBuilder();
        for (int i = 2; i < split.length; i++){
            town
                    .append(split[i])
                    .append(" ");
        }
        if (town.length() > 0) {
            town.deleteCharAt(town.length() - 1);
        }
        return town.toString();
    }

    private String updateUser(Town town, User user){
        if (town == null){
            return "Город не найден";
        }
        user.setTown(town);
        userService.update(user);
        return "Город успешно установлен";
    }
}

package org.example.weather.service;

import com.vk.api.sdk.objects.messages.Message;
import lombok.RequiredArgsConstructor;
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
                String town = parseTown(split);
                Integer townId = townService.get(town);
                return updateUser(townId, user);
            }
            default:
                return "Команда не распознана";
        }
    }

    private String parseTown(String[] split){
        StringBuilder town = new StringBuilder();
        for (int i = 2; i < split.length; i++){
            town
                    .append(split[i])
                    .append(" ");
        }
        if (town.length() > 0) {
            town.deleteCharAt(town.length() - 1);
        }
        return townService.toString();
    }

    private String updateUser(Integer townId, User user){
        if (townId == null){
            return "Город не найден";
        }
        user.setTownId(townId);
        userService.update(user);
        return "Город успешно установлен";
    }
}

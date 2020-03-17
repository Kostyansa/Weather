package org.example.weather.service;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.queries.messages.MessagesGetLongPollHistoryQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class VKService {

    private int ts;

    private final VkApiClient vkApiClient;
    private final GroupActor groupActor;
    private final CommandService commandService;

    @PostConstruct
    private void updateTs() throws ClientException, ApiException {
        ts =  vkApiClient.messages()
                .getLongPollServer(groupActor)
                .execute()
                .getTs();
    }

    public void sendMessage(String message, Integer userId, Integer messageId){
        try {
            vkApiClient
                    .messages()
                    .sendWithUserIds(groupActor, new Integer[]{userId})
                    .randomId(messageId)
                    .message(message)
                    .execute();
            log.trace("Sent a {} to {} with message ID: {}", message, userId, messageId);
        }
        catch (ApiException | ClientException e) {
            log.debug("There was an Exception: ", e);
        }
    }

    @Scheduled(fixedDelay = 3000)
    public void getMessages(){
        MessagesGetLongPollHistoryQuery eventsQuery = vkApiClient
                .messages()
                .getLongPollHistory(groupActor)
                .ts(ts);
        try {
            List<Message> messages = eventsQuery.execute().getMessages().getItems();
            if (messages.isEmpty()){
                return;
            }
            updateTs();
            for (Message message : messages){
                response(message);
            }
        } catch (ApiException | ClientException e) {
            log.debug("There was an Exception: ", e);
        }
    }

    @Async
    private void response(Message message){
        String response = commandService.execute(message);
        sendMessage(response, message.getFromId(), message.getId());
    }


}
